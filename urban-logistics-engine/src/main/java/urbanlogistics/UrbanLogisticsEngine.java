/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package urbanlogistics;

/**
 *
 * @author franc
 */
import java.nio.file.Paths;
import urbanlogistics.ui.MainMenu;

public class UrbanLogisticsEngine {

    public static void main(String[] args) {
        new MainMenu(Paths.get("data", "state.txt")).run();
    }
}
