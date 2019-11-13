import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class SearchInfoPanel extends JPanel {

	private ButtonGroup radioGroup;
	private JLabel duration;
	public SearchInfoPanel(Main frame) {
		setPreferredSize(new Dimension(300, 800));
		setLayout(new FlowLayout());
		
		duration = new JLabel();
		duration.setText("전체 조회");
		add(duration);
		
		JSpinner dateStartSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
		JSpinner dateEndSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
		dateStartSpinner.setEditor(new JSpinner.DateEditor(dateStartSpinner, "yyyy-MM-dd"));
		dateEndSpinner.setEditor(new JSpinner.DateEditor(dateEndSpinner, "yyyy-MM-dd"));
		
		add(dateStartSpinner);
		add(dateEndSpinner);
		
		JButton dateSearch = new JButton("검색");
		dateSearch.addActionListener(e -> {
			if(frame.getTableName().length()==0) {
				JOptionPane.showMessageDialog(null, "데이터를 열어주세요.");
				return;
			}
			frame.init(); //((Driver)getParent().getParent().getParent().getParent()).init(); 로 쓸수도 있음
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String start = sdf.format(dateStartSpinner.getValue());
			String end = sdf.format(dateEndSpinner.getValue());
			duration.setText(start + " ~ " + end);
			frame.setDatas(Request.openData(frame.getTableName(), start, end));
		});
		add(dateSearch);
		
		JRadioButton[] radios = {new JRadioButton("년"), new JRadioButton("월")};
		radioGroup = new ButtonGroup();
		ItemListener listener = e -> {
			if(frame.getTableName().length()==0 || e.getStateChange() == ItemEvent.DESELECTED) return; // 해제 이벤트 종료
			frame.setTableDatas(((JRadioButton)e.getItem()).getText());
		};
		for(JRadioButton radio : radios) {
			radio.addItemListener(listener);
			radioGroup.add(radio);
			add(radio);
		}
	}
	
	public void init() {
		radioGroup.clearSelection();
		duration.setText("전체 조회");
	}
}
