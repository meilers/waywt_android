package com.sobremesa.waywt.listeners;



public interface LoginListener {
	public void onLoginSuccess();
	public void onLoginFailure(Exception exception);
}

