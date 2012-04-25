package com.pandacoder.MireaWallpaper;

import java.util.Iterator;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.eightbitmage.gdxlw.LibdgxWallpaperListener;
import com.pandacoder.MireaWallpaper.Wallpaper.MireaWallpaperEngine;
import com.pandacoder.MireaWallpaper.Shapes.Ring;
import com.pandacoder.MireaWallpaper.Utils.CirclesList;
import com.pandacoder.MireaWallpaper.Utils.CirclesList.MyCircle;
import com.pandacoder.MireaWallpaper.Utils.RingTexureBank;


public class WallpaperGraphicsEngine implements ApplicationListener, LibdgxWallpaperListener {
	
	private static final String LOG_TAG = "MireaWallpaper.Wallpaper1Engine";
	private static final String PREFS_NAME = LOG_TAG;
	private static final String CIRCLES_RADIUS_ARRAY_PREFF_KEY = "CirclesArray";

	private MireaWallpaperEngine engine;
	final Context appContext;
	
	//private Stage stage;

	private Texture mBg;
	private Sprite mMireaLettering;
	private SpriteBatch mBatch;
	private Ring mRing;
	private RingTexureBank ringTextures;	
	//private Label fps;
	
	private int	  mWidth = 0;
	private int	  mHeight = 0;
	private float mOffsetX = 0;
	private float mRingCenterX = 0, mRingCenterY = 0;
	
	private final float 	mCircleMinThickness;
	private final float 	mCircleBorderRadius;
	private final float 	mPixelScaleCoef;
	
	private final CirclesList	mActiveCircles = new CirclesList();		// сюда ложим круги, которые надо нарисовать
	private final CirclesList	mInactiveCircles = new CirclesList();	// сюда ложим лишние круги, чтобы не создавать заного, когда понадобятся
	
	/**
	 * Создает графический движок обоев. 
	 * @param appContext - контекст, чтобы получить доступ к ресурсам
	 * @param engine - движок обоев, чтобы узнавать ориентацию экрана
	 */
	public WallpaperGraphicsEngine(final Context appContext, final MireaWallpaperEngine engine) {
		
		this.engine = engine;
		this.appContext = appContext;
		
		final Resources res = appContext.getResources();
		mCircleMinThickness 	 = res.getDimension(R.dimen.circle_min_thickness);
		mCircleBorderRadius		 = res.getDimension(R.dimen.circle_border_radius);
		mPixelScaleCoef			 = res.getDimension(R.dimen.pixel_scale_coef);
	}


	/**
	 * Вызывается при создании обоев
	 */
	@Override
	public void create() {
		Log.i(LOG_TAG, "created");
	
		mWidth = Gdx.graphics.getWidth();
		mHeight = Gdx.graphics.getHeight();
		
		ringTextures = new RingTexureBank();
		//stage = new Stage(mWidth, mHeight, false);
		
		mBatch = new SpriteBatch();
		
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("packedimages/pack"));
		
		final int minScreenDim = Math.min(mHeight, mWidth);
		String mireaTextureName = "mireab";
		if (minScreenDim < 250) mireaTextureName = "mireas";
		else if (minScreenDim < 370) mireaTextureName = "miream";  
		mMireaLettering = atlas.createSprite(mireaTextureName);
		// хоть и выводим текстуру пиксель в пиксель, всеравно ставим линейные фильтры,
		// так как при прокручивании работчего стола текстура сдвигается на пол пикселя 
		// например, и становится некрасиво... поэтому чтобы прокрутка была плавнее
		// включаем филтры, чтботы текструа выглядела красиво
		mMireaLettering.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

		mBg = new Texture(Gdx.files.internal("data/background.png"));
		mBg.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		mRing = new Ring();
		mRing.setColor(0xCC00FFFF);
		
		//fps = new Label("fps", new BitmapFont(), "fps: 0");
	    //fps.x = 10; fps.y = 100;
	    //fps.color.set(0, 1, 0, 1);
		
		//stage.addActor(fps);
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		UpdateDimentions();
		
		mActiveCircles.LoadFromString(appContext.getSharedPreferences(PREFS_NAME, 0).getString(CIRCLES_RADIUS_ARRAY_PREFF_KEY, ""));
		if (mActiveCircles.size() == 0)	mActiveCircles.add(new MyCircle());
	}
	
	public final static int DEFAULT_FRAME_INTERVAL = 40;	
	private float maxCircleRadius;
	
	/**
	 * Вызывается при рендеренги очередного кадра обоев
	 */
	@Override
	public void render() {
		
		// смотрим сколько времени прошло между кадрами
		// если надо притормаживаем обои, чтобы батарейку зря не жрали
		int deltaTime = (int) (Gdx.graphics.getDeltaTime() * 1000);
		if (deltaTime > DEFAULT_FRAME_INTERVAL) deltaTime = DEFAULT_FRAME_INTERVAL;
		int timeToSleep = DEFAULT_FRAME_INTERVAL - deltaTime;
		if (timeToSleep > 0) {	// если работаем слишком быстро
			try {
				Thread.sleep(timeToSleep);	// притормозим и поспим
			} catch (InterruptedException e) {}
		}
				
		// начинаем рисовать
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, mWidth, mHeight);
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		mBatch.begin();
			// рисуем фон
			mBatch.draw(mBg, 0, 0, mWidth, mHeight, 0, 0, (float)mWidth/16, (float)mHeight/16);
			// рисуем надпись МИРЭА
			mMireaLettering.draw(mBatch);
		mBatch.end();
		
		
		/*((Label)stage.findActor("fps")).setText("fps: " + Gdx.graphics.getFramesPerSecond() 
														+ " aX = " + Gdx.input.getAccelerometerX() 
														+ " aY = " + Gdx.input.getAccelerometerY() 
														+ " aZ = " + Gdx.input.getAccelerometerZ()
														+ " orientation: " + engine.getOrientation());
		stage.draw();*/
		
		// начинаем рисовать кольца

		// считываем данные с нужной оси акселерометра, в зависимости от
		// ориентации устройства
		float accel;
		switch (engine.getOrientation()) {
		case 0: accel = Gdx.input.getAccelerometerX(); break;
		case 1: accel = -Gdx.input.getAccelerometerY(); break;
		case 2: accel = -Gdx.input.getAccelerometerX(); break;
		case 3: accel = Gdx.input.getAccelerometerY(); break;
		default: accel = 0;
		}
		// деформируем кольца согласно данным с акселерометра
		mRing.setDeformation(accel/30);

		boolean addNewCircle = false;	// надо создать новый круг, если тру )
		for (Iterator<MyCircle> it = mActiveCircles.iterator(); it.hasNext(); ) {	// перебираем все активные круги					
			
			final MyCircle circle = (MyCircle) it.next(); 
			
			float radiusStep = (circle.radius/500.f + 0.18f) * deltaTime/DEFAULT_FRAME_INTERVAL * mPixelScaleCoef;	// рассчитываем приращение радиуса
			if (circle.radius < mCircleBorderRadius && circle.radius + radiusStep >= mCircleBorderRadius) {	// если круг пересекает определенную границу - надо добавить еще один
				addNewCircle = true;
			}				
			
			circle.radius += radiusStep;	// новый радиус круга
			final float circleThickness = circle.radius/16.f + mCircleMinThickness;	// толщина круга
			
			// задаем кольцу параметры: положение, радиус и толщину
			mRing.setParams(mRingCenterX, mRingCenterY, circle.radius, circleThickness);
			mRing.setTexture(ringTextures.get(circleThickness)); 	// задаем тектуру
			mRing.draw(); 											// рисуем кольцо
			
			if (circle.radius - circleThickness - 1 > maxCircleRadius) {	// если круг уже слишком большой и выходит за границы экрана
				it.remove();						// убираем его из активных
				mInactiveCircles.add(circle);		// и добавляем в список неактивных
			}
		}
		
		if (addNewCircle == true) {			// нужно запустить новый круг
			final MyCircle newCircle;
			if (mInactiveCircles.size() > 0) {		// если есть неактивные круги
				newCircle = mInactiveCircles.remove(0);	// берем первый из них
				newCircle.radius = 0;					// задаем начальный радиус
			} else {
				newCircle = new MyCircle();		// если неактивных кругов нет - создаем новый
			}
			mActiveCircles.add(newCircle);	// добавляем  новый круг в список активных
		}
	}
	
	/**
	 * Рассчитывае всякие параметры геометрии, нужно вызавать при инициализации и при изменении
	 * размеров экрана и тд.
	 */
	private void UpdateDimentions() {
		
		// это рамеры нашего экрана
		mWidth = Gdx.graphics.getWidth();
		mHeight = Gdx.graphics.getHeight();
		
		Log.i("Wallpaper1Engine", "Updating dimentions: " + mWidth + ", " + mHeight);
		
		/*if (stage != null) {	// если задана сцена
			// корректируем его согласно новым размерам
			stage.setViewport(mWidth, mHeight, false);
		}*/
						
		if (mBatch != null) { // если есть SpriteBatch
			// корректируем его параметры согласно новым размерам
			mBatch.getProjectionMatrix().setToOrtho2D(0, 0, mWidth, mHeight);
		}
		
		// рассчитываем положение надписи МИРЭА
		UpdateMireaLetteringParams();		
	}
	
	/**
	 * Рассчитывает положение надписи МИРЭА и некоторых параметры колец
	 */
	private void UpdateMireaLetteringParams() {
		
		// если надпись есть
		if (mMireaLettering != null) {
			
			final float width = mMireaLettering.getWidth();
			final float height = mMireaLettering.getHeight();
			
			final float left = mWidth/2 - width/2 - (mOffsetX - 0.5f) * mWidth / 8.0f;
			final float top = mHeight/2 - height/2;
			
			// задаем новое положение надписи
			mMireaLettering.setPosition(left, top);
			
			// а это новые центры колец
			mRingCenterX = (left + width * 0.117f);
			mRingCenterY = (top + height * 0.84f);
			
			// еще посчитаем какого максимального радиуса может существовать
			// кольцо в текущей геометрии
			final float maxX = Math.max(mWidth - mRingCenterX, mRingCenterX);
			final float maxY = Math.max(mHeight - mRingCenterY, mRingCenterY);
			
			// запомним максимальны радиус
			maxCircleRadius = (float) Math.sqrt(maxX*maxX + maxY*maxY);
		}
	}

	/**
	 * Вызывается, если сменилось смещение обоев, нужно подвинуть надпись
	 */
	@Override
	public void offsetChange(float xOffset, float yOffset, float xOffsetStep,
			float yOffsetStep, int xPixelOffset, int yPixelOffset) {
		
		// обновляем изначение смещения вроль оси Х, другие параметры нам не нужны
		mOffsetX = xOffset;		
		
		// обновляем координаты надписи МИРЭА
		UpdateMireaLetteringParams();
	}

	@Override
	public void setIsPreview(boolean isPreview) {
		
	}

	@Override
	public void dispose() {
		Log.i(LOG_TAG, "DISPOSE event");
	}

	@Override
	public void pause() {
		Log.i(LOG_TAG, "PAUSE event");
	}

	@Override
	public void resize(int arg0, int arg1) {
		UpdateDimentions();
		reloadTextures();
	}

	@Override
	public void resume() {
		Log.i(LOG_TAG, "RESUME event");
		reloadTextures();
	}
	
	/**
	 * Повторно загружает все используемые в обоях текстуры. 
	 * Понадобится при потере гл контекста, вызывать будем из
	 * resume и resize, как просит документация
	 */
	private void reloadTextures() {
		
		// регенерирует наши текстуры для колец
		ringTextures.regenerateTextures();
		
		// регенерирует все managed мекстуры, поидеи они должны сами
		// перезагружаться, но иногда они этого не делают, поэтому 
		// вызываем сами
		Texture.invalidateAllTextures(Gdx.app);
	}
	
	/**
	 * Сохраняет состояние
	 */
	public void saveEngineState() {
		appContext.getSharedPreferences(PREFS_NAME, 0).edit()
			.putString(CIRCLES_RADIUS_ARRAY_PREFF_KEY, mActiveCircles.SaveToString())
			.commit();
	}
}
