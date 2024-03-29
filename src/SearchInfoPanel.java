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
	public SearchInfoPanel(Main frame) {
		setPreferredSize(new Dimension(260, 800));
		setLayout(new FlowLayout());
		
		JSpinner dateStartSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
		JSpinner dateEndSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
		dateStartSpinner.setEditor(new JSpinner.DateEditor(dateStartSpinner, "yyyy-MM-dd"));
		dateEndSpinner.setEditor(new JSpinner.DateEditor(dateEndSpinner, "yyyy-MM-dd")); // 날짜입력 스피너 생성
		
		add(dateStartSpinner);
		add(dateEndSpinner);
		
		JButton dateSearch = new JButton("검색");
		dateSearch.addActionListener(e -> { // 검색버튼 이벤트 처리
			if(frame.getTableName().length()==0) {
				JOptionPane.showMessageDialog(null, "데이터를 열어주세요.");
				return;
			}
			frame.init();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String start = sdf.format(dateStartSpinner.getValue());
			String end = sdf.format(dateEndSpinner.getValue());
			frame.setDatas(Request.openData(frame.getTableName(), start, end));
		});
		add(dateSearch);
		
		JRadioButton[] radios = {new JRadioButton("전체"), new JRadioButton("년"), new JRadioButton("월")};
		radioGroup = new ButtonGroup();
		ItemListener listener = e -> { // 기간 지정 라디오버튼 이벤트 처리
			if(frame.getTableName().length()==0 || e.getStateChange() == ItemEvent.DESELECTED) return; // 해제 이벤트 종료
			frame.setTableDatas(((JRadioButton)e.getItem()).getText());
		};
		for(JRadioButton radio : radios) {
			radio.addItemListener(listener);
			radioGroup.add(radio);
			add(radio);
		}
	}
	
	public void init() { // 검색옵션 상태 초기화
		radioGroup.clearSelection();
	}
}
