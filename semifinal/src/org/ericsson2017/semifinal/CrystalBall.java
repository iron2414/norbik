package org.ericsson2017.semifinal;

import java.util.Arrays;
import java.util.function.BiFunction;
import org.ericsson2017.protocol.semifinal.ResponseClass;

public class CrystalBall {
	private final int HEIGHT=80;
	private final int WIDTH=100;
	
	//1. y
	//2. x
	public final boolean[][] cells=new boolean[HEIGHT][WIDTH];
	//1. t
	//2. y
	//3. x
	public float[][][] probabilities=new float[0][HEIGHT][WIDTH];
	public int timeLimit;
	
	public static float add(float p0, float p1) {
		return (1.0f-(1.0f-p0)*(1.0f-p1));
	}
	
	public void addEnemies(Iterable<Enemy> enemies) {
		enemies.forEach(this::addEnemy);
	}
	
	public void addEnemiesReader(Iterable<ResponseClass.Enemy.Reader> enemies) {
		enemies.forEach(this::addEnemy);
	}
	
	public void addEnemy(int xx, int yy, int dx, int dy, float probability,
			int time) {
		if ((timeLimit<=time)
				|| (0>xx)
				|| (0>yy)
				|| (WIDTH<=xx)
				|| (HEIGHT<=yy)) {
			return;
		}
		probabilities[time][yy][xx]
				=add(probabilities[time][yy][xx], probability);
		int fx=xx+dx;
		int fy=yy+dy;
		if (isFree(fx, fy)) {
			addEnemy(fx, fy, dx, dy, probability, time+1);
			return;
		}
		int ldx=-dy;
		int ldy=dx;
		int lx=xx+ldx;
		int ly=yy+ldy;
		int rdx=dy;
		int rdy=-dx;
		int rx=xx+rdx;
		int ry=yy+rdy;
		int bdx=-dx;
		int bdy=-dy;
		int bx=xx+bdx;
		int by=yy+bdy;
		boolean lf=isFree(lx, ly);
		boolean rf=isFree(rx, ry);
		boolean bf=isFree(bx, by);
		if (lf && rf) {
			if (bf) {
				float pp=0.33333333333333f*probability;
				addEnemy(lx, ly, ldx, ldy, pp, time+1);
				addEnemy(rx, ry, rdx, rdy, pp, time+1);
				addEnemy(bx, by, bdx, bdy, pp, time+1);
			}
			else {
				float pp=0.5f*probability;
				addEnemy(lx, ly, ldx, ldy, pp, time+1);
				addEnemy(rx, ry, rdx, rdy, pp, time+1);
			}
			return;
		}
		if (lf) {
			if (bf) {
				float pp=0.5f*probability;
				addEnemy(lx, ly, ldx, ldy, pp, time+1);
				addEnemy(bx, by, bdx, bdy, pp, time+1);
			}
			else {
				addEnemy(lx, ly, ldx, ldy, probability, time+1);
			}
			return;
		}
		if (rf) {
			if (bf) {
				float pp=0.5f*probability;
				addEnemy(rx, ry, rdx, rdy, pp, time+1);
				addEnemy(bx, by, bdx, bdy, pp, time+1);
			}
			else {
				addEnemy(rx, ry, rdx, rdy, probability, time+1);
			}
			return;
		}
		int lfx=(lx+fx)/2;
		int lfy=(ly+fy)/2;
		int rfx=(rx+fx)/2;
		int rfy=(ry+fy)/2;
		int lbx=(lx+bx)/2;
		int lby=(ly+by)/2;
		int rbx=(rx+bx)/2;
		int rby=(ry+by)/2;
		boolean lff=isFree(lbx, lby);
		boolean rff=isFree(rbx, rby);
		boolean lbf=isFree(lbx, lby);
		boolean rbf=isFree(rbx, rby);
		if ((!bf) && (!lff) && (!rff)) {
			if (lbf) {
				if (rbf) {
					float pp=0.5f*probability;
					addEnemy(lbx, lby, bdx, bdy, probability, time+1);
					addEnemy(rbx, rby, bdx, bdy, probability, time+1);
				}
				else {
					addEnemy(lbx, lby, bdx, bdy, probability, time+1);
				}
				return;
			}
			if (rbf) {
				addEnemy(rbx, rby, bdx, bdy, probability, time+1);
				return;
			}
		}
		if (bf) {
			if (lff) {
				if (rff) {
					float pp=0.33333333333333f*probability;
					addEnemy(lfx, lfy, ldx, ldy, pp, time+1);
					addEnemy(rfx, rfy, rdx, rdy, pp, time+1);
					addEnemy(bx, by, bdx, bdy, pp, time+1);
				}
				else {
					float pp=0.5f*probability;
					addEnemy(lfx, lfy, ldx, ldy, pp, time+1);
					addEnemy(bx, by, bdx, bdy, pp, time+1);
				}
			}
			else if (rff) {
				float pp=0.5f*probability;
				addEnemy(rfx, rfy, rdx, rdy, pp, time+1);
				addEnemy(bx, by, bdx, bdy, pp, time+1);
			}
			else {
				addEnemy(bx, by, bdx, bdy, probability, time+1);
			}
		}
		else {
			if (lff) {
				if (rff) {
					float pp=0.5f*probability;
					addEnemy(lfx, lfy, ldx, ldy, pp, time+1);
					addEnemy(rfx, rfy, rdx, rdy, pp, time+1);
				}
				else {
					addEnemy(lfx, lfy, ldx, ldy, probability, time+1);
				}
			}
			else if (rff) {
				addEnemy(rfx, rfy, rdx, rdy, probability, time+1);
			}
			else {
			}
		}
	}
	
	public void addEnemy(Enemy enemy) {
		int dx;
		switch (enemy.dirX) {
			case LEFT:
				dx=-1;
				break;
			case RIGHT:
				dx=1;
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"invalid enemy direction x %1$s", enemy.dirX));
		}
		int dy;
		switch (enemy.dirY) {
			case DOWN:
				dy=1;
				break;
			case UP:
				dy=-1;
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"invalid enemy direction y %1$s", enemy.dirY));
		}
		addEnemy(enemy.coord.y, enemy.coord.x, dx, dy, 1.0f, 0);
	}
	
	public void addEnemy(ResponseClass.Enemy.Reader enemy) {
		int dx;
		switch (enemy.getDirection().getHorizontal()) {
			case LEFT:
				dx=-1;
				break;
			case RIGHT:
				dx=1;
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"invalid enemy direction x %1$s",
						enemy.getDirection().getHorizontal()));
		}
		int dy;
		switch (enemy.getDirection().getVertical()) {
			case DOWN:
				dy=1;
				break;
			case UP:
				dy=-1;
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"invalid enemy direction y %1$s",
						enemy.getDirection().getVertical()));
		}
		addEnemy(enemy.getPosition().getY(), enemy.getPosition().getX(),
				dx, dy, 1.0f, 0);
	}
	
	private boolean isFree(int xx, int yy) {
		return (0<=xx)
				&& (0<=yy)
				&& (WIDTH>xx)
				&& (HEIGHT>yy)
				&& cells[yy][xx];
	}
	
	public void reset(BiFunction<Integer, Integer, Boolean> cellFree,
			int timeLimit) {
		this.timeLimit=timeLimit;
		if (probabilities.length<timeLimit) {
			probabilities=new float[timeLimit][HEIGHT][WIDTH];
		}
		else {
			for (float[][] a0: probabilities) {
				for (float[] a1: a0) {
					Arrays.fill(a1, 0.0f);
				}
			}
		}
		for (int yy=HEIGHT-1; 0<=yy; --yy) {
			for (int xx=WIDTH-1; 0<=xx; --xx) {
				this.cells[yy][xx]=cellFree.apply(xx, yy);
			}
		}
	}
	
	public void reset(ResponseClass.Response.Reader response, int timeLimit) {
		reset(
				(xx, yy)->0==response.getCells().get(yy).get(xx).getOwner(),
				timeLimit);
	}
	
	public void reset(ServerResponseParser response, int timeLimit) {
		reset(
				(xx, yy)->0==response.cells[yy][xx],
				timeLimit);
	}
	
	public float sumTTXY(int fromT, int toT, int xx, int yy) {
		float result=0.0f;
		for (int tt=fromT; toT>tt; ++tt) {
			result=add(result, probabilities[tt][yy][xx]);
		}
		return result;
	}
}
