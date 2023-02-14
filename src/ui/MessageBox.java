package ui;

import java.awt.Font;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import xml_ui.Observable;
import xml_ui.attributes.BindingAttribute;
import xml_ui.attributes.EventCallbackAttribute;
import xml_ui.attributes.NamedComponentAttribute;
import xml_ui.controls.Window;
import xml_ui.exceptions.InvalidXMLException;

public class MessageBox extends Window
{
    //#region Static
    /**
     * Shows a message box with the specified title, header and message.
     * @param title
     * @param header
     * @param message
     */
    public static void Show(String title, String header, String message)
    {
        try
        {
            MessageBox messageBox = new MessageBox(title, header, message, EMessageBoxButtons.OK);
            messageBox.Show();
        }
        catch (Exception e)
        {
            System.err.println("Failed to show message box: " + e.getMessage());
        }
    }

    /**
     * Shows a message box with the specified title, header, message and buttons and waits for the user to close the message box.
     * @param title
     * @param header
     * @param message
     * @param buttons {@link ui.EMessageBoxButtons}
     * @return The button that was clicked {@link ui.EMessageBoxButtons}
     */
    public static int ShowDialog(String title, String header, String message, int buttons)
    {
        try
        {
            MessageBox messageBox = new MessageBox(title, header, message, buttons);
            messageBox.ShowDialog();
            return messageBox.GetResult();
        }
        catch (Exception e)
        {
            System.err.println("Failed to show message box: " + e.getMessage());
            return EMessageBoxButtons.NONE;
        }
    }
    //#endregion

    //#region Instance
    @BindingAttribute(DefaultValue = Themes.LIGHT_BACKGROUND_PRIMARY)
    private Observable<String> backgroundColour;
    @BindingAttribute(DefaultValue = Themes.LIGHT_FOREGROUND)
    private Observable<String> foregroundColour;

    @NamedComponentAttribute
    private JLabel header;
    @NamedComponentAttribute
    private JLabel message;
    @NamedComponentAttribute
    private JButton yesButton;
    @NamedComponentAttribute
    private JButton noButton;
    @NamedComponentAttribute
    private JButton okButton;
    @NamedComponentAttribute
    private JButton cancelButton;

    private int result = EMessageBoxButtons.NONE;

    protected MessageBox(String title, String header, String message, int buttons)
        throws IOException, ParserConfigurationException, SAXException, InvalidXMLException, IllegalArgumentException, IllegalAccessException
    {
        super();

        Window.SetTitle(rootComponent, title);
        this.header.setText(header);
        this.message.setText(message);

        if ((buttons & EMessageBoxButtons.YES) == EMessageBoxButtons.YES)
            yesButton.setVisible(true);
        if ((buttons & EMessageBoxButtons.NO) == EMessageBoxButtons.NO)
            noButton.setVisible(true);
        if ((buttons & EMessageBoxButtons.OK) == EMessageBoxButtons.OK)
            okButton.setVisible(true);
        if ((buttons & EMessageBoxButtons.CANCEL) == EMessageBoxButtons.CANCEL)
            cancelButton.setVisible(true);

        //Set the message font to be light, by default labels are bold.
        this.message.setFont(new Font(
            this.message.getFont().getName(),
            Font.PLAIN,
            this.message.getFont().getSize()));

        rootComponent.pack();
    }

    @EventCallbackAttribute
    private void yesButton_Click(Object[] args)
    {
        result = EMessageBoxButtons.YES;
        rootComponent.dispose();
    }

    @EventCallbackAttribute
    private void noButton_Click(Object[] args)
    {
        result = EMessageBoxButtons.NO;
        rootComponent.dispose();
    }

    @EventCallbackAttribute
    private void okButton_Click(Object[] args)
    {
        result = EMessageBoxButtons.OK;
        rootComponent.dispose();
    }

    @EventCallbackAttribute
    private void cancelButton_Click(Object[] args)
    {
        result = EMessageBoxButtons.CANCEL;
        rootComponent.dispose();
    }

    public int GetResult()
    {
        return result;
    }
    //#endregion
}