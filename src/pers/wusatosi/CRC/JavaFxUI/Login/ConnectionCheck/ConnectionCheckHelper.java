package pers.wusatosi.CRC.JavaFxUI.Login.ConnectionCheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import pers.wusatosi.CRC.CRCApi.ConnectionCheck;
import pers.wusatosi.CRC.JavaFxUI.UIEventProcesser;

public final class ConnectionCheckHelper implements Observable{
	
	//For FXML
	public ConnectionCheckHelper() {}
	
	private ConnectionCheckHelper(Consumer<IOException> con, Pane root) {
		this.con = con;
		
		root.setVisible(true);
		
		FXMLLoader fxmll = new FXMLLoader();
		fxmll.setLocation(getClass().getResource("ConnectionCheck.fxml"));
		fxmll.setController(this);
		fxmll.setRoot(root);
		try {
			fxmll.load();
		} catch (IOException e1) {
			throw new Error("Failed To load ConnectionCheck.fxml",e1);
		}
		
		addListener((b)->{
			root.setVisible(false);
		});
		
		StringBuffer info = new StringBuffer("校系统连接延迟:");
		AtomicInteger count = new AtomicInteger();
		Consumer<Integer> after = (Info) ->{
			info.append(" " + Info + "ms");
			ProgressS.setProgress(ProgressS.getProgress() + 0.5);
			if (count.get() == 0) {
				SCCText.setText(info.toString());
				obscd.countDown();
			}
		};
		action(SCCText , ConnectionCheck::portalCheck, count, after);
		action(SCCText , ConnectionCheck::portalCheck, count, after);
		action(CCText, ConnectionCheck::baiduCheck, null, (delay) -> {
			CCText.setText("外网延迟: "+delay);
			ProgressO.setProgress(1);
			obscd.countDown();
		});
		obscd.addFinishListener((value) -> {
			UIEventProcesser.getInstance()
				.submit(()-> {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						throw new Error(e);
					}
				})
				.setOnSucceededReallyFriendly((a)-> FireEvents());
		});
	}
	
	private Consumer<IOException> con;
	private ObservableCountDown obscd = new ObservableCountDown(3);

	private static class waitHelper{
		
		private static CountDownLatch counter;
		
		private static volatile List<Text> updateList = new ArrayList<>(2);
		
		public static void await(Text text) throws InterruptedException {
			updateList.add(text);
			checkCounter();
			
			counter.await();
			
			updateList.clear();
			counter = null;
		}
		
		private static void checkCounter() {
			if (counter == null) {
				//start timer
				counter = new CountDownLatch(5);
				UIEventProcesser.getInstance().execute(() -> {
					updateList.forEach((text) -> {
						Platform.runLater(() -> text.setText("系统连接失败"));
					});
					for (;counter.getCount() > 0;counter.countDown()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						updateList.forEach((text) -> {
							final int currentCount = (int) counter.getCount();
							Platform.runLater(() -> text.setText(currentCount+ "秒后重试"));
						});
					}
				});
			}
		}
		
	}
	
    @FXML
    private volatile Text SCCText;

    @FXML
    private volatile ProgressIndicator ProgressS;
    
    private interface aConnectionCheck{
    	
    	int get() throws IOException;
    	
    }
    
    @FXML
    private volatile Text CCText;

    @FXML
    private volatile ProgressIndicator ProgressO;
    
	private void action(Text text, aConnectionCheck check, AtomicInteger count, Consumer<Integer> after) {
		UIEventProcesser.getInstance().submit(() -> {
			Boolean isBlocked = false;
			for (;;) {
				try {
					int value = check.get();
					if (isBlocked && count != null) count.decrementAndGet();
					return value;
				}catch (IOException e) {
					if (!isBlocked) {
						isBlocked = true;
						if (count != null) count.incrementAndGet();
					}
					Platform.runLater(()->{
						con.accept(e);
					});
					waitHelper.await(text);
				}
			}
		})
			.setDoLetUncaughtExceptionHandlerHandleThrowableExceptException(true)
			.setOnSucceededReallyFriendly((info) -> after.accept(info.getPayload()))
			.setOnFailedByExceptionReallyFriendly((e) -> {
				Thread thread = Thread.currentThread();
				thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
			});
	}
    
    class ObservableCountDown {

		public ObservableCountDown(int count) {
			obs = new SimpleIntegerProperty(count);
		}
		
		private SimpleIntegerProperty obs;
		
		private SimpleBooleanProperty obsb;

		public void countDown() {
			if (obs.get() != 0) obs.set(obs.get() - 1);
			if (obs.get() == 0 && obsb != null)
				obsb.setValue(true);
		}
		
		public ObservableIntegerValue getCountProperty() {
			return this.obs;
		}
    	
		public void addFinishListener(InvalidationListener listener) {
			if (obsb == null)
				obsb = new SimpleBooleanProperty(obs.get() == 0);
			obsb.addListener(listener);
		}
		
    }
    
	public static ConnectionCheckHelper BuildConnectionHelper(Consumer<IOException> e, Pane root) {
		return new ConnectionCheckHelper(e, root);
	}
	
	@Override
	public void addListener(InvalidationListener listener) {
		Objects.requireNonNull(listener);
		listen.add(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		Objects.requireNonNull(listener);
		listen.remove(listener);
	}
	
	private List<InvalidationListener> listen = new ArrayList<>(5);
	
	private void FireEvents() {
		listen.forEach((listener) -> {
			try {
				listener.invalidated(null);
			}catch (Throwable e) {
				Thread current = Thread.currentThread();
				current.getUncaughtExceptionHandler().uncaughtException(current, e);
			}
		});
	}
	
}
