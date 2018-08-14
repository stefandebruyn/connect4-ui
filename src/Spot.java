import java.net.URL;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public final class Spot {
    private JLabel label;
    private SpotState state;
    
    public Spot(JLabel label) {
        this.label = label;
        label.setText("");
        
        setState(SpotState.EMPTY);
    }
    
    public void setState(SpotState state) {
        this.state = state;
        
        String path = "";
        
        switch (state) {
            case RED:
                path = "/red.png";
            break;
                
            case RED_WIN:
                path = "/red-win.png";
            break;
                
            case BLUE:
                path = "/blue.png";
            break;
                
            case BLUE_WIN:
                path = "/blue-win.png";
            break;
                
            case EMPTY:
                path = "/empty.png";
            break;
        }
        
        URL source = getClass().getResource(path);
        
        if (source != null)
            label.setIcon(new ImageIcon(source));
    }
    
    public SpotState getState() { return state; }
}
