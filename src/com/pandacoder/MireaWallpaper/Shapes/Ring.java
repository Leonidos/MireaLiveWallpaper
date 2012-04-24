package com.pandacoder.MireaWallpaper.Shapes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

/**
 * Класс для отображения кольца средствами LibGDX.
 * Используется аппроксимация треугольниками в 2 ряда (внешний и внутрений)
 * @author Leonidos
 *
 */
public class Ring {
	
	private static final int RING_QUOTE_APROXIMATION_PIECES = 24;
	private static final int RING_APROXIMATION_PIECES = RING_QUOTE_APROXIMATION_PIECES * 4;
	private static float quoteCircleCoord[];
	private static float[] vertices;	// вершины треугольников составляющих кольцо

	private Texture texture;	// текстура для наложения на кольцо
	private Mesh diskMesh;		// сетка для нашего кольца 
	private float[] color;
	
	// параметры: положение x,y, толщина,        радиус
	private float x = 0, y = 0, thickness = 10, radius = 100;
	private boolean dirtyVertices = true;	// если true надо пересчитать вершины
	private float deformationMatrix[];
	
	/**
	 * Создает и инициализирует кольцо
	 * @param pieces - количество внешних треугольников для апроксимации
	 */
	public Ring() {
		init();
	}
	
	/**
	 * Инициализирует ресурсы
	 */
	private void init() {
		
		if (quoteCircleCoord == null) {
			
			final int pointsCnt = RING_QUOTE_APROXIMATION_PIECES * 2 + 1;
			final double stepRad = Math.PI / RING_QUOTE_APROXIMATION_PIECES / 4;
			
			quoteCircleCoord = new float[2*pointsCnt];
			
			for (int i = 0; i < pointsCnt; i++) {
				quoteCircleCoord[2*i+0] = (float) Math.cos(i * stepRad);	// внешняя сторона Х
				quoteCircleCoord[2*i+1] = (float) Math.sin(i * stepRad);	// внешняя сторона У
			}
			
			vertices = new float[(RING_APROXIMATION_PIECES * 4 + 4)*2];
		}
		// сетка для кольца
		diskMesh =  new Mesh(false, 
				(RING_APROXIMATION_PIECES * 2 + 2)*2,	// число вершин 
				0, 					// число индексов (нам не нужны)
				new VertexAttribute(Usage.Position, 2, "ringVerices"),	// будем передавать 2 координаты вершин
				new VertexAttribute(Usage.TextureCoordinates, 2, "texCoords"));	// 2 координаты для текстуры		
	}
	
	public void setDeformation(float coef) {
		
		if (coef < -0.3) coef = -0.3f;
		if (coef > 0.3f) coef = 0.3f;
 		
		if (deformationMatrix == null) {
			deformationMatrix = new float[16];
			deformationMatrix[0] = deformationMatrix[5] = deformationMatrix[10] = deformationMatrix[15] = 1;
		}
			
		deformationMatrix[1] = coef;
	}
	
	/**
	 * Рассчитывает вершины для треугольников аппроксимирующих кольцо,
	 * рассчитывает координаты для наложения текстуры, настраивает
	 * сетку
	 */
	private void updateVertices() {

		// обычные координаты на внешней и внутренней стороне кольца для
		// треугольников, которыми будем его аппроксимировать
		int i, j;
		for (i = 0, j = 0; i < RING_APROXIMATION_PIECES; i++) {
			vertices[8*i+2] = (j)&0x1;		// а это координаты для наложения тектуры
			vertices[8*i+3] = (j>>1)&0x1;	  j++; 	      
			vertices[8*i+6] = (j)&0x1;		// а это координаты для наложения текстуры
			vertices[8*i+7] = (j>>1)&0x1;    j++;
		}
		
		final int qPieces = RING_QUOTE_APROXIMATION_PIECES;
		for (i = 0; i <= 2 * qPieces; i+=2) {
			vertices[4*i+0] = radius * quoteCircleCoord[2*i + 0];
			vertices[4*i+1] = radius * quoteCircleCoord[2*i + 1];
		}

		for (i = 1; i < 2 * qPieces; i+=2) {
			vertices[4*i+0] = (radius - thickness) * quoteCircleCoord[2*i + 0];	// внутренняя сторона Х
			vertices[4*i+1] = (radius - thickness) * quoteCircleCoord[2*i + 1];	// внутренняя сторона У
		}
		
		for (i = 0; i <= 2*qPieces; i++) {
			vertices[4*(4 * qPieces - i)+0] = -vertices[4*i+0];	// внешняя сторона Х
			vertices[4*(4 * qPieces - i)+1] = vertices[4*i+1];	// внешняя сторона У
			
			vertices[4*(4 * qPieces + i)+0] = -vertices[4*i+0];	// внешняя сторона Х
			vertices[4*(4 * qPieces + i)+1] = -vertices[4*i+1];	// внешняя сторона У
			
			vertices[4*(8 * qPieces - i)+0] = vertices[4*i+0];	// внешняя сторона Х
			vertices[4*(8 * qPieces - i)+1] = -vertices[4*i+1];	// внешняя сторона У
		}
				
		i = RING_APROXIMATION_PIECES;
		// чтобы кольцо получилось замкнутым, нужно добавить еще треугольников
		// координаты которых совпадают с координатами первых треугольников
		vertices[8*i+0] = vertices[0];
		vertices[8*i+1] = vertices[1];
		vertices[8*i+2] = vertices[2];
		vertices[8*i+3] = vertices[3];
		vertices[8*i+4] = vertices[4];
		vertices[8*i+5] = vertices[5];
		vertices[8*i+6] = vertices[6];
		vertices[8*i+7] = vertices[7];
		
		// применяем к сетке новые координаты вершин и наложения текстуры
		diskMesh.setVertices(vertices);
	}
	
	/**
	 * Устанавливает новую текстуру для кольца, чтобы убрать текстуру
	 * передать null (не вызывает dispose у старой текcтуры)
	 * @param texture - текстура
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	/**
	 * Устанавливает новый цвет кольца в формате ARGB. Чтобы отключить 
	 * цвет нужно передать -1
	 * @param color - цвет 0xAARRGGBB
	 */
	public void setColor(int colorARGB) {
		
		if (colorARGB == -1) {
			color = null;
			return;
		}
		
		if (color == null) color = new float[4];
		
		color[0] = ((colorARGB>>16)&0xFF)/255f; 
		color[1] = ((colorARGB>>8) &0xFF)/255f; 
		color[2] = ((colorARGB>>0) &0xFF)/255f;
		color[3] = ((colorARGB>>24)&0xFF)/255f;
	}

	/**
	 * Задает параметры кольца
	 * @param x - центр Х
	 * @param y - центр У
	 * @param radius - радиус
	 * @param thickness - толщина
	 */
	public void setParams(float x, float y, float radius, float thickness) {
		
		// если заданы новые радиус или толщина, надо пересчитать координаты вершин
		if (this.radius != radius || this.thickness != thickness) dirtyVertices = true;
		
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.thickness = thickness;
	}
	
	/**
	 * Выводит кольцо
	 */
	public void draw() {
		
		GL10 gl = Gdx.gl10;
		
		if (dirtyVertices == true) {	// если вершины грязные
			updateVertices();			// пересчитываем вершины
			dirtyVertices = false;		// говорим, что они чистые
		}
				
	    gl.glPushMatrix();
	    
		if (texture != null) gl.glEnable(GL10.GL_TEXTURE_2D);	// если есть текстура, включаем
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glTranslatef(x, y, 0);	// переносим центр кольца в заданнуют точку
		
		// если задана матрица деформации, применяем ее
		if (deformationMatrix != null) gl.glMultMatrixf(deformationMatrix, 0);
				
		if (color != null)     gl.glColor4f(color[0], color[1], color[2], color[3]);
		
		if (texture != null) {
			texture.bind();	// биндим текстуру, если есть
		}
		diskMesh.render(GL10.GL_TRIANGLE_STRIP, 0, RING_APROXIMATION_PIECES * 2 + 2);	// выводим кольцо

		if (texture != null) gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
		
		gl.glPopMatrix();
	}
	
	/**
	 * Очищает ресурсы. Вызвать если кольцо больше не нужно.
	 */
	public void dispose() {
		diskMesh.dispose();
		if (texture != null) texture.dispose();
	}
}
