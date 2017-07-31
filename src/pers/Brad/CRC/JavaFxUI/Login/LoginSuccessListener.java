package pers.Brad.CRC.JavaFxUI.Login;

import java.util.EventListener;

import pers.Brad.CRC.InternetAcessPart.loginedUser;

public interface LoginSuccessListener extends EventListener{
	public void loginEvent(loginedUser user);
}
