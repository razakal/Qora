package gui.models;

import java.util.List;

import javax.swing.DefaultComboBoxModel;

import qora.voting.PollOption;

@SuppressWarnings("serial")
public class OptionsComboBoxModel extends DefaultComboBoxModel<PollOption> {

	public OptionsComboBoxModel(List<PollOption> options)
	{
		for(PollOption option: options)
		{
			this.addElement(option);
		}
	}
}
