package pers.wusatosi.CRC.JavaFxUI;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public final class UIEventProcesser extends ThreadPoolExecutor {

	private UIEventProcesser() {
		super(1, Integer.MAX_VALUE,20L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
	}
	
	private static final UIEventProcesser INSTANCE = new UIEventProcesser();
	
	public static UIEventProcesser getInstance() {
		return INSTANCE;
	}
	
	public final static <T> FriendlyTask<T> quickSubmit(Callable<T> task){
		return getInstance().submit(task);
	}
	
	public final static FriendlyTask<Void> quickSubmit(Runnable task){
		return getInstance().submit(task);
	}

	@Override
	protected <T> FriendlyTask<T> newTaskFor(Callable<T> callable) {
		return new FriendlyTask<T>(callable);
	}
	
	@Override
	protected <T> FriendlyTask<T> newTaskFor(Runnable runnable, T value) {
		return new FriendlyTask<>(() -> {
			runnable.run();
			return value;
		});
	}

	@Override
	public <T> FriendlyTask<T> submit(Callable<T> task) {
		return (FriendlyTask<T>) super.submit(task);
	}

	@SuppressWarnings("unchecked")
	@Override
	public FriendlyTask<Void> submit(Runnable task) {
		return (FriendlyTask<Void>) super.submit(task);
	}

	@Override
	public <T> FriendlyTask<T> submit(Runnable task, T result) {
		return (FriendlyTask<T>) super.submit(task, result);
	}
	
	public final class FriendlyTask<T> extends Task<T>{
		
		public FriendlyTask(Callable<T> target){
			this.Target = target;
		}
		
		private Callable<T> Target;
		
		public FriendlyTask<T> setOnRunningReallyFriendly(EventHandler<WorkerStateEvent> value) {
			this.setOnRunning(value);
			return this;
		}
		
		public FriendlyTask<T> setOnScheduledReallyFriendly(EventHandler<WorkerStateEvent> value) {
			this.setOnScheduled(value);
			return this;
		}
		
		public FriendlyTask<T> setOnSucceededReallyFriendly(Consumer<InfoPackage<T>> value) {
			Objects.requireNonNull(value);
			this.setOnSucceeded((state) -> {
				T info;
				try {
					info = get();
				} catch (InterruptedException | ExecutionException e) {
					throw new Error(e);
				}
				value.accept(new InfoPackage<>(info, this));
			});
			return this;
		}
		
		public FriendlyTask<T> setOnFailedReallyFriendly(Consumer<InfoPackage<Throwable>> value) {
			Objects.requireNonNull(value);
			this.setOnFailed((state) -> value.accept(new InfoPackage<>(getException(), this)));
			return this;
		}
		
		public FriendlyTask<T> setOnFailedByExceptionReallyFriendly(Consumer<Exception> value){
			Objects.requireNonNull(value);
			this.setOnFailed((state) ->{
				Throwable t = getException();
				if (t instanceof Exception)
					value.accept((Exception) t);
			});
			return this;
		}
		
		public FriendlyTask<T> setOnCancelledReallyFriendly(EventHandler<WorkerStateEvent> value) {
			this.setOnCancelled(value);
			return this;
		}
		
		public final FriendlyTask<T> setDoLetUncaughtExceptionHandlerHandleThrowableExceptException(Boolean value){
			if (value)
				exceptionProperty().addListener(LetThreadUncaughtExceptionHandlerHandleThrowablExceptException);
			else
				exceptionProperty().removeListener(LetThreadUncaughtExceptionHandlerHandleThrowablExceptException);
			return this;
		}
		
		public void retryInUIEventProcesser() { 
		}
		
		private InvalidationListener LetThreadUncaughtExceptionHandlerHandleThrowablExceptException
				= (state) -> {
					Throwable t;
					if (!((t = getException()) instanceof Exception)) {
						Thread current = Thread.currentThread();
						current.getUncaughtExceptionHandler().uncaughtException(current, t);
					}
				};
				
				
		public class InfoPackage<R>{
			
			R info;
			
			FriendlyTask<T> This;

			public InfoPackage(R info, FriendlyTask<T> this1) {
				this.info = info;
				This = this1;
			}
			
			public R getPayload() {
				return info;
			}
			
			public FriendlyTask<T> getThis(){
				return This;
			}
			
		}


		@Override
		protected T call() throws Exception {
			return Target.call();
		}
		
	}
	
}
