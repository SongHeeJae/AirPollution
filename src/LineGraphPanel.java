import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class LineGraphPanel extends GraphPanel{

	public LineGraphPanel(String start, String end, int pol) {
		super(start, end, pol);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Color[] color = {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.ORANGE};
		
		if(!places.isEmpty() && 50 < x && x <= 50 + datas.size()/places.size()*100 && x/100*100-2 <= x && x <= x/100*100+2 && 40 <= y && y <= 590)
			for(int i=0; i<places.size(); i++) {
				int y = (int)(590 - 550/(max/datas.get((x/100-1) + datas.size()/places.size()*i)));
				g.drawLine(50, y, (int)getPreferredSize().getWidth(), y);
				g.drawString(places.get(i) + " - " + Double.toString(datas.get((x/100-1) + datas.size()/places.size()*i)), x+5, y-10);
			} // 마우스의 x좌표가 그래프의 x좌표에 닿았을때의 처리
		
		if(50 <= x && x <= (int)getPreferredSize().getWidth() && 40 <= y && y <= 590)
			g.drawLine(x,40,x, 590);
		
		((Graphics2D)g).setStroke(new BasicStroke(2));
		for(int i=0; i<places.size(); i++) { // 꺾은선 그래프 그리기
			g.setColor(color[i]);
			g.drawString("●" + places.get(i), (i+1)*100, 645);
			int y = (int)(590 - 550/(max/datas.get(i * datas.size()/places.size()))), yy;
			g.fillRect(100, y-2, 4, 4);
			int k = 1;
			for (int j=i * datas.size()/places.size() + 1; j<(i+1) * datas.size() / places.size(); j++) {
				yy = y;
				y = (int)(590 - 550/(max/datas.get(j)));
				g.fillRect((k+1)*100-2, y-2, 4, 4);
				g.drawLine((k)*100, yy, (1+k++)*100, y);
			}
		}
		
		if(!datas.isEmpty()) { // 열린데이터가 있으면 가로축 그려줌
			g.setColor(Color.BLACK);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				cal.setTime(sdf.parse(start));
				for(int i=1; i<=datas.size()/places.size(); i++) {
					g.drawString(sdf.format(cal.getTime()), i*100-20, 620);
					cal.add(Calendar.DATE, 1);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void setResize() {
		if(!datas.isEmpty() && datas.size()/places.size() * 100 + 50 > getPreferredSize().getWidth())
			setPreferredSize(new Dimension(datas.size()/places.size() * 100 + 100, 650));
		else if (datas.isEmpty()) setPreferredSize(new Dimension(950, 650));
		reload();
	}
	
	public void removeGraph(String place) {
		int pos = -1;
		for (int i=0; i<places.size(); i++)
			if(places.get(i).equals(place)) {
				pos = i;
				break;
			}
		int i = pos * datas.size()/places.size(), j = datas.size()/places.size();
		while(j-- > 0) datas.remove(i);
		places.remove(pos);
		setResize();
	}
}
