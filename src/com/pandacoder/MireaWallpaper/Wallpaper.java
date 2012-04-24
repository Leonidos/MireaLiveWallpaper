package com.pandacoder.MireaWallpaper;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.livewallpaper.AndroidApplicationLW;
import com.eightbitmage.gdxlw.LibdgxWallpaperService;


/**
 * Класс-сервис обоев
 * @author Leonidos
 *
 */
public class Wallpaper extends LibdgxWallpaperService {
	
	// ссылочка на дисплей, чтобы потом побысстрому узнавать параметры ориентации
	private Display mDisplay = null;
	
	/**
	 * Отдаем новый энджин обоев
	 */
	@Override
	public Engine onCreateEngine() {
		return new MireaWallpaperEngine(this);
	}
	
	/**
	 * Узнает текущую ориентацию экрана, можно было бы закешировать значение, но 
	 * onConfigurationChanged у меня не всегда вызывается. Если экран просто менял 
	 * ориентацию, но не менял размера, изменения конфигурации не происходило. 
	 * Поэтому всегда изнаем у дисплея текущую ориентацию. 
	 */
	private int getOrientation() {
		if (mDisplay == null) {
			WindowManager mWindowManager =  (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			mDisplay = mWindowManager.getDefaultDisplay();
		}
	    		
		// тут в зависимости от версии андроида надо вызывать нужную функцию (именно из-за места
		// этого проект собирается для андроид 2.2, хотя работает начиная от 2.1) 
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) return mDisplay.getOrientation();
		return mDisplay.getRotation();		
	}
	
	/**
	 * Движок обоев
	 * @author Leonidos
	 *
	 */
	public class MireaWallpaperEngine extends LibdgxWallpaperEngine {

		public MireaWallpaperEngine(LibdgxWallpaperService libdgxWallpaperService) {
			super(libdgxWallpaperService);			
		}
		
		/**
		 * Возвращает ориентацию экрана. Просто пробрасываем дальше, то что отдает {@link Wallpaper#getOrientation}
		 * @return ориентация экрана, значение от 0-3
		 */
		public int getOrientation() {
			return Wallpaper.this.getOrientation();
		}
		
		/**
		 * Инициализирует энжин
		 */
		@Override
		protected void initialize(AndroidApplicationLW androidApplicationLW) {
		
			// создаем графический движок
			WallpaperGraphicsEngine gEngine = new WallpaperGraphicsEngine(Wallpaper.this.getApplicationContext(), MireaWallpaperEngine.this);
			
			// он у нас слушает события специфичные для обоев
			setWallpaperListener(gEngine);
			
			// и заобно реагирует на разные события приложения
			androidApplicationLW.initialize(gEngine, false);
		}

		/**
		 * Вызывает перед уничтожением обоев
		 */
		@Override
		public void onDestroy() {
			if (DEBUG)
				Log.d(TAG, " > MyEngine() " + hashCode());
			
			super.onDestroy();			
			
			//сохраним состояние обоев, чтобы кажды раз не начинать с начала
			((WallpaperGraphicsEngine)wallpaperListener).saveEngineState();
		}
	}
}
