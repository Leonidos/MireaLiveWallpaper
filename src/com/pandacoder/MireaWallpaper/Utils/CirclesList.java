package com.pandacoder.MireaWallpaper.Utils;

import java.util.ArrayList;

/**
 * Массив окружностестей, поддерживает распаковку и упаковку в строку
 * @author Leonidos
 *
 */
@SuppressWarnings("serial")
public class CirclesList extends ArrayList<CirclesList.MyCircle> {

	/**
	 * Класс-окружность заданного радиуса
	 * @author Leonidos
	 *
	 */
	public static class MyCircle {
		
		public MyCircle() {}
		public MyCircle(float radius) {
			this.radius = radius;
		}
		
		public float radius = 0;
	}

	/**
	 * Сохраняет радиусы окружностей в строку
	 * @returns строка
	 */
	public String SaveToString() {
		StringBuilder radiuses = new StringBuilder();
		for (MyCircle circle:this) {
			radiuses.append(circle.radius);
			radiuses.append(';');
		}
		return radiuses.toString();
	} 
	
	/**
	 * Загружает окружности из строки
	 * 
	 */
	public void LoadFromString(final String data) {		
		if (data != null && data.length() > 0) {
			final String [] radiuses = data.split(";");
			for (String r:radiuses)
				add(new MyCircle(Float.parseFloat(r)));
		}		
	}
}
