import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class BarGraphPanel extends GraphPanel {
	
	private double[] standard;
	
	public BarGraphPanel(String start, String end, int pol) {
		super(start, end, pol);
		switch(pol) {
		case 0:
			standard = new double[] {0.02, 0.05, 0.15};
			break;
		case 1:
			standard = new double[] {0.03, 0.09, 0.15};
			break;	
		case 2:
			standard = new double[] {0.45, 1, 2};
			break;
		case 3:
			standard = new double[] {0.03, 0.06, 0.2};
			break;
		case 4:
			standard = new double[] {15, 35, 75};
			break;
		default:
			standard = new double[] {30, 80, 100};
			break;
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Color[] color = new Color[] {Color.BLUE, Color.GREEN, Color.ORANGE, Color.RED};
		
		for(int i=0; i<color.length; i++) {
			g.setColor(color[i]);
			if(i == 0) g.drawString("● 좋음", (i+1)*100, 645);
			else if (i == 1) g.drawString("● 보통", (i+1)*100, 645);
			else if (i == 2) g.drawString("● 나쁨", (i+1)*100, 645);
			else g.drawString("● 매우나쁨", (i+1)*100, 645);
		}
		
		for (int i=0; i<datas.size(); i++) { // 막대그래프 그리기
			int y = datas.get(i) == 0 ? 590 : (int)(590 - 550/(max/datas.get(i)));
			int c=0;
			while(c < standard.length && datas.get(i) > standard[c++]);
			g.setColor(color[c - 1]);
			g.fillRect((i+1)*100, y, 50, 590-y);
			g.setColor(Color.BLACK);
			g.drawString(places.get(i), (i+1)*100, 620);
		}
		
		if(50 <= x && x <= (int)getPreferredSize().getWidth() && 40 <= y && y <= 590)
			g.drawLine(x,40,x, 590);
		
		if(50 < x && x <= 50 + places.size()*100 && x/100*100 <= x && x <= x/100*100+50 && 40 <= y && y <= 590) {
			int y = (int)(590 - 550/(max/datas.get(x/100-1)));
			g.drawLine(50, y, (int)getPreferredSize().getWidth(), y);
			g.drawString(Double.toString(datas.get(x/100-1)), x+5, y-10);
		} // 마우스 x좌표가 그래프의 x좌표에 닿았을때의 처리
	}
	

	
	public void setResize() {
		if(datas.size() > 6)
			setPreferredSize(new Dimension(900 + (datas.size()-6) * 100, 650));
		else setPreferredSize(new Dimension(900, 650));
		reload();
	}
	
	
	public void removeGraph(String place) {
		int pos = -1;
		for (int i=0; i<places.size(); i++)
			if(places.get(i).equals(place)) {
				pos = i;
				break;
			}
		
		datas.remove(pos);
		places.remove(pos);
		setResize();
	}
	

}
