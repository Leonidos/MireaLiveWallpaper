package com.pandacoder.MireaWallpaper.Utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Банк текстур для сглаживания колец. Хранит и побыстрому отдает текстуры для сглаживания колец
 * различной толщины
 * @author Leonidos
 *
 */
public class RingTexureBank {
	
	/**
	 * Тип границы для сглаживания
	 * @author Leonidos
	 *
	 */
	private enum BorderType {
		/**
		 * Основая
		 */
		BASE, 
		/**
		 * Переходная. Маскирует переход между основными.
		 */
		TRANSIENT;
	}
	
	/**
	 * Объекты с информацией для сглаживания
	 * @author Leonidos
	 *
	 */
	private enum BorderTextures {
		
		upto10(10, 4, BorderType.BASE), 
		upto20(20, 5, BorderType.TRANSIENT), 
		upto30(30, 5, BorderType.BASE), 
		upto45(45, 6, BorderType.TRANSIENT), 
		upto60(60, 6, BorderType.BASE);
		
		private Texture texture;
		private int size2power, validWidth; 
		private BorderType borderType;
		
		/**
		 * Объект с информацией для сглаживания круга. Саму тектура тенерируется не тут.
		 * @param validWidth допустимая толщина < validWidth
		 * @param size2power размер текстуры 2^size2power
		 * @param borderType тип границы сглаживания
		 */
		private BorderTextures(int validWidth, int size2power, BorderType borderType) {
			this.size2power = size2power;
			this.borderType = borderType;
			this.validWidth = validWidth;
		}
	}
	
	/**
	 * содержит кеш перечесления текстур для круга, чтобы get быстро искал по текстурам
	 */
	private BorderTextures[] borderTexturesCache;
	
	/**
	 * Банк текстур для сглаживания круга. Быстро отдает нам разные текстуры для опреледенной толщины круга.
	 * Инициализирует кэш текстур для сглаживания и создает сами текстуры
	 */
	public RingTexureBank() {
		borderTexturesCache = BorderTextures.values();
		for (BorderTextures t : borderTexturesCache) {
			t.texture = generateTexture(t.size2power, t.borderType);
		}
	}
	
	/**
	 * Регенерирует текстуры для сглаживания кругов. Так как текстуры кругов не
	 * управляемые (unManaged), то их надо регенерировать при переключении контекста
	 */
	public void regenerateTextures() {
		for (BorderTextures t : borderTexturesCache) {
			if (t.texture != null) t.texture.dispose();
			t.texture = generateTexture(t.size2power, t.borderType);
		}
	}
	
	/**
	 * Отдает текстуру для сглаживания 
	 * @param width толщина круга
	 * @return тектура
	 */
	public Texture get(float width) {

		for (BorderTextures t : borderTexturesCache) {
			if (width < t.validWidth) {
				return t.texture;
			}
		}
		
		return borderTexturesCache[borderTexturesCache.length - 1].texture;		
	}
		
	/**
	 * Гератор текстур для сглаживания
	 * @param size2power размер 2^size2power
	 * @param borderType тип границы
	 * @return текстура
	 */
	static private Texture generateTexture(final int size2power, BorderType borderType) {		

		if (size2power < 3) throw new IllegalArgumentException("size2power should be more or equals than 4");
				
		final int pixmapWidth = (int) Math.pow(2, size2power); 
		final float alfaBorderValues[] = getAlfaValues(borderType);
		final int borderWidth = alfaBorderValues.length;
		
		final Pixmap pixmap = new Pixmap(pixmapWidth, 1, Pixmap.Format.Alpha);
		pixmap.setColor(0, 0, 0, 1f);
		pixmap.drawLine(borderWidth, 0, pixmapWidth - borderWidth, 0);
			
		for (int i = 0; i < borderWidth; i++) {		
			pixmap.setColor(0, 0, 0, alfaBorderValues[i]);
			pixmap.drawPixel(i, 0);
			pixmap.drawPixel(pixmapWidth - i - 1, 0);
		}
		
		return new Texture(pixmap, false);
	}		
	
	static private float [] getAlfaValues(BorderType borderType) {
		
		if (borderType == BorderType.BASE) return new float[] {0.2f, 0.7f, 0.9f};		
		return new float[] {0.2f, 0.58f, 0.80f, 0.88f, 0.90f};
	}
}
