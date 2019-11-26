import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class BarGraphPanel extends GraphPanel {
	
	public BarGraphPanel(String start, String end, int pol) {
		super(start, end, pol);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if(50 < x && x <= 50 + places.size()*100 && x/100*100 <= x && x <= x/100*100+50 && 40 <= y && y <= 590) {
			int y = (int)(590 - 550/(max/datas.get(x/100-1)));
			g.drawLine(50, y, (int)getPreferredSize().getWidth(), y);
			g.drawString(Double.toString(datas.get(x/100-1)), x+5, y-10);
		} // ���콺 x��ǥ�� �׷����� x��ǥ�� ��������� ó��
		
		for (int i=0; i<datas.size(); i++) { // ����׷��� �׸���
			int y = (int)(590 - 550/(max/datas.get(i)));
			g.drawRect((i+1)*100, y, 50, 590-y);
			g.drawString(places.get(i), (i+1)*100, 620);
		}
	}
	

	
	public void setResize() {
		if(datas.size() * 125 + 50 > getPreferredSize().getWidth())
			setPreferredSize(new Dimension((int)getPreferredSize().getWidth() + 125, 650));
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
