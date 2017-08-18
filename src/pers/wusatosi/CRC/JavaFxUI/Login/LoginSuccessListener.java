package pers.Brad.CRC.JavaFxUI.Login;

import java.util.function.Consumer;

import pers.Brad.CRC.CRC.loginedUser;

public interface LoginSuccessListener extends Consumer<loginedUser>{
	public void loginEvent(loginedUser user);
}
