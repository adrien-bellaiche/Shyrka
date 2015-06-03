package display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import node.Observer;
import classic.Client;

public class ButtonBar extends JPanel implements ActionListener {


	private static final long serialVersionUID = -2016845425361322629L;
	private JButton bu;
	private JButton nTurn;
	private int pfollowed=1;

	public ButtonBar() {
		this.setLayout(null);
		bu =new JButton("Following Player " + pfollowed);
		nTurn=new JButton("Turn : " + Client.round);
		this.setSize(1000,30);
		bu.setBounds(0, 0, 500, 30);
		nTurn.setBounds(500, 0, 495, 30);
		bu.setVisible(true);
		nTurn.setVisible(true);
		this.add(bu);
		this.add(nTurn);
		bu.addActionListener(this);
		nTurn.addActionListener(this);
		this.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if(arg0.getSource()==bu) {
			System.out.println("Following Player " + pfollowed);
			pfollowed=3-pfollowed;
			for(Observer obs : Observer.observers) {
				obs.follow(pfollowed);
			}
			bu.setText("Following Player " + pfollowed);
			bu.repaint();
		}  else  { //can only be nturn or bu, so here it is nturn
			System.out.println("Next turn requested");
			for(Observer obs : Observer.observers) {
				obs.nextTurn();
			}
			nTurn.setText("Turn : " + Client.round);
			nTurn.repaint();
		}
	}

}
