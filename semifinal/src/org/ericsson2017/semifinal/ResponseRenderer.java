package org.ericsson2017.semifinal;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.ericsson2017.protocol.semifinal.ResponseClass;

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
	private final ResponseClass.Response.Reader response;
	private final int width;

	private ResponseRenderer(Graphics2D graphics, int height,
			ResponseClass.Response.Reader response, int width) {
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
				response.getInfo().getLevel(),
				response.getInfo().getTick(),
				response.getInfo().getOwns());
		drawString(
				"status: %1$s",
				response.getStatus());
		for (int ee=0; response.getEnemies().size()>ee; ++ee) {
			ResponseClass.Enemy.Reader enemy=response.getEnemies().get(ee);
			drawString(
					"e %1$,3d  %2$,3dx%3$,3d %4$6sx%5$6s",
					ee,
					enemy.getPosition().getX(),
					enemy.getPosition().getY(),
					enemy.getDirection().getHorizontal(),
					enemy.getDirection().getVertical());
		}
		for (int uu=0; response.getUnits().size()>uu; ++uu) {
			ResponseClass.Unit.Reader unit=response.getUnits().get(uu);
			drawString(
					"u %1$,3d  o: %2$,1d  h: %3$,1d  k: %4$,2d  %5$,3d x %6$,3d %7$6s",
					uu,
					unit.getOwner(),
					unit.getHealth(),
					unit.getKiller(),
					unit.getPosition().getX(),
					unit.getPosition().getY(),
					unit.getDirection());
		}
		cellsWidth=response.getCells().get(0).size();
		cellsHeight=response.getCells().size();
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
				ResponseClass.Cell.Reader cell
						=response.getCells().get(yy).get(xx);
				if (0!=cell.getOwner()) {
					graphics.setColor(color(cell.getOwner()));
					graphics.fillRect(cx+1, cy+1, cellSize-1, cellSize-1);
				}
				graphics.setColor(Color.LIGHT_GRAY);
				switch (cell.getAttack().which()) {
					case CAN:
						break;
					case UNIT:
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
						break;
					default:
						throw new IllegalArgumentException(
								cell.getAttack().which().toString());
				}
			}
		}
		for (ResponseClass.Enemy.Reader enemy: response.getEnemies()) {
			int ex=cellsX+enemy.getPosition().getY()*cellSize;
			int ey=cellsY+enemy.getPosition().getX()*cellSize;
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.drawOval(
					ex+1,
					ey+1,
					cellSize-2,
					cellSize-2);
		}
		for (ResponseClass.Unit.Reader unit: response.getUnits()) {
			int ux=cellsX+unit.getPosition().getY()*cellSize;
			int uy=cellsY+unit.getPosition().getX()*cellSize;
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
			ResponseClass.Response.Reader response) {
		new ResponseRenderer(graphics, height, response, width)
				.render();
	}
}
