package org.ericsson2017.semifinal;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResponseRenderer {
	private static final List<Color> COLORS=Collections.unmodifiableList(
			Arrays.asList(
					Color.RED,
					Color.GREEN,
					Color.BLUE,
					Color.WHITE,
					Color.YELLOW,
					Color.CYAN,
					Color.MAGENTA,
					Color.BLACK));
	private static final Font FONT=new Font("Monospaced", 0, 12);
	private static final int PADDING=3;
	
	private final CrystalBall ball;
	private int cellSize;
	private int cellsHeight;
	private int cellsWidth;
	private int cellsX;
	private int cellsY;
	private final FontMetrics fontMetrics;
	private final Graphics2D graphics;
	private final int height;
	private int line;
	private int lineWidth;
	private final ServerResponseParser response;
	private final int width;

	private ResponseRenderer(CrystalBall ball, Graphics2D graphics,
            int height, ServerResponseParser response, int width) {
        this.ball=ball;
		this.graphics=graphics;
		this.height=height;
		this.response=response;
		this.width=width;
		graphics.setBackground(Color.DARK_GRAY);
		graphics.setFont(FONT);
		fontMetrics=graphics.getFontMetrics();
	}
	
	private Color color(int owner) {
		return COLORS.get((owner+COLORS.size()-1)%COLORS.size());
	}
	
	private void drawString(String format, Object... args) {
		String string=String.format(format, args);
		graphics.drawString(string, PADDING,
				line*(fontMetrics.getHeight()+PADDING)
						+fontMetrics.getAscent()+PADDING);
		++line;
		lineWidth=Math.max(lineWidth, fontMetrics.stringWidth(string));
	}
	
	private void render() {
		graphics.setColor(graphics.getBackground());
		graphics.fillRect(0, 0, width, height);
		if (null==response) {
			return;
		}
		graphics.setColor(Color.LIGHT_GRAY);
		drawString(
				"level: %1$,8d  tick: %2$,8d  owns: %3$,8d",
				response.infoLevel,
				response.infoTick,
				response.infoOwns);
		drawString(
				"status: %1$s",
				response.status);
		for (int ee=0; response.getEnemies().size()>ee; ++ee) {
			Enemy enemy=response.enemies.get(ee);
			drawString(
					"e %1$,3d  %2$,3dx%3$,3d %4$6sx%5$6s",
					ee,
					enemy.coord.x,
					enemy.coord.y,
					enemy.dirX,
					enemy.dirY);
		}
		for (int uu=0; response.getUnits().size()>uu; ++uu) {
			Unit unit=response.getUnits().get(uu);
			drawString(
					"u %1$,3d  o: %2$,1d  h: %3$,1d  k: %4$,2d  %5$,3d x %6$,3d %7$6s",
					uu,
					unit.owner,
					unit.health,
					unit.killer,
					unit.coord.x,
					unit.coord.y,
					unit.dir);
		}
		cellsWidth=response.cells[0].length;
		cellsHeight=response.cells.length;
		cellSize=Math.min((width-1-3*PADDING-lineWidth)/cellsWidth,
				(height-1-2*PADDING)/cellsHeight);
		cellsX=width-PADDING-1-cellsWidth*cellSize;
		cellsY=PADDING;
		for (int rr=cellsHeight; 0<=rr; --rr) {
			graphics.drawLine(
					cellsX,
					cellsY+rr*cellSize,
					cellsX+cellsWidth*cellSize,
					cellsY+rr*cellSize);
		}
		for (int cc=cellsWidth; 0<=cc; --cc) {
			graphics.drawLine(
					cellsX+cc*cellSize,
					cellsY,
					cellsX+cc*cellSize,
					cellsY+cellsHeight*cellSize);
		}
		for (int xx=cellsWidth-1; 0<=xx; --xx) {
			int cx=cellsX+xx*cellSize;
			for (int yy=cellsHeight-1; 0<=yy; --yy) {
				int cy=cellsY+yy*cellSize;
				int cell=response.cells[yy][xx];
				if (0!=cell) {
					graphics.setColor(color(cell));
					graphics.fillRect(cx+1, cy+1, cellSize-1, cellSize-1);
				}
				graphics.setColor(Color.LIGHT_GRAY);
				if (0<=response.attackUnits[yy][xx]) {
					graphics.drawLine(
							cx+1,
							cy+1,
							cx+cellSize-2,
							cy+cellSize-2);
					graphics.drawLine(
							cx+cellSize-2,
							cy+1,
							cx+1,
							cy+cellSize-2);
				}
				graphics.setColor(Color.MAGENTA);
				float probability=ball.sumTTXY(0, ball.timeLimit, xx, yy);
				if (0.0f<probability) {
					int size=Math.round(probability*(cellSize-1));
					int offset=(cellSize-1-size)/2;
					graphics.fillOval(cx+1+offset, cy+1+offset, size, size);
				}
			}
		}
		for (Enemy enemy: response.getEnemies()) {
			int ex=cellsX+enemy.coord.y*cellSize;
			int ey=cellsY+enemy.coord.x*cellSize;
			graphics.setColor(Color.WHITE);
			graphics.drawOval(
					ex+1,
					ey+1,
					cellSize-2,
					cellSize-2);
		}
		for (Unit unit: response.getUnits()) {
			int ux=cellsX+unit.coord.y*cellSize;
			int uy=cellsY+unit.coord.x*cellSize;
			graphics.setColor(color(unit.getOwner()));
			graphics.fillOval(
					ux+1,
					uy+1,
					cellSize-2,
					cellSize-2);
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.drawOval(
					ux+1,
					uy+1,
					cellSize-2,
					cellSize-2);
		}
	}
	
	public static void render(Graphics2D graphics, int width, int height,
			CrystalBall ball, ServerResponseParser response) {
		new ResponseRenderer(ball, graphics, height, response, width)
				.render();
	}
}
