import javafx.application.Application;


import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.collections.FXCollections;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Random;
import java.util.Properties;
import java.util.List;

import java.time.*;
import java.time.format.*;

public class TimeSheeter extends Application {
	Stage mainStage = null;

	Pane mainLayoutPane = null;
	Pane minLayoutPane = null;

	Scene mainScene = null;
	Scene minScene = null;


	Button btn_Minimize = null;
	ChoiceBox<String> projectSelector = null;
	TextField txt_notes = null;
	Button btn_StartStop = null;
	Button btn_Refresh = null;
	Button btn_ViewFolder = null;

	Button btn_Maximize = null;

	boolean isRecording = false;
	String startTime = "";

	@Override
	public void start(Stage primaryStage) {

		// Create base stage as a utility (no taskbar)
		// and make it "invisible"
		primaryStage.initStyle(StageStyle.UTILITY);
		primaryStage.setOpacity(0);
        primaryStage.setHeight(0);
        primaryStage.setWidth(0);
        primaryStage.show();


        // Create the stage we will actually display this will be chromeless
		mainStage = new Stage();
		mainStage.initOwner(primaryStage);
        mainStage.initStyle(StageStyle.UNDECORATED);
		mainStage.setAlwaysOnTop(true);




		// Set up and layout our main window
		mainLayoutPane = new Pane();
		mainLayoutPane.getStylesheets().add("application.css");
		mainScene = new Scene(mainLayoutPane, 195, 145);

		btn_Minimize = new Button(" ");
		btn_Minimize.setId("btn-Minimize");
		mainLayoutPane.getChildren().add(btn_Minimize);
		btn_Minimize.addEventHandler(MouseEvent.MOUSE_CLICKED,
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent e) {
			goMinimized();
	      }
        });



		btn_ViewFolder = new Button("🖿");
		btn_ViewFolder.setLayoutX(15);
		btn_ViewFolder.setLayoutY(8);
		btn_ViewFolder.setPrefWidth(80);
		btn_ViewFolder.setId("btn-ViewFolder");
		mainLayoutPane.getChildren().add(btn_ViewFolder);
		btn_ViewFolder.addEventHandler(MouseEvent.MOUSE_CLICKED,
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent e) {
			try{
				Runtime.getRuntime().exec("explorer " + System.getProperty("user.dir") + "\\data");
			}
			catch(Exception exc){}
	      }
        });


		btn_Refresh = new Button("↺");
		btn_Refresh.setLayoutX(105);
		btn_Refresh.setLayoutY(8);
		btn_Refresh.setPrefWidth(80);
		btn_Refresh.setId("btn-ViewFolder");
		mainLayoutPane.getChildren().add(btn_Refresh);
		btn_Refresh.addEventHandler(MouseEvent.MOUSE_CLICKED,
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent e) {
			try{
				loadProjectList();
			}
			catch(Exception exc){}
	      }
        });


		projectSelector = new ChoiceBox<String>();
		mainLayoutPane.getChildren().add(projectSelector);
		projectSelector.setLayoutX(15);
		projectSelector.setLayoutY(40);
		projectSelector.setPrefWidth(170);
		loadProjectList();


		txt_notes = new TextField ();
		mainLayoutPane.getChildren().add(txt_notes);
		txt_notes.setLayoutX(15);
		txt_notes.setLayoutY(73);
		txt_notes.setPrefWidth(170);


		btn_StartStop = new Button("START");
		btn_StartStop.setLayoutX(15);
		btn_StartStop.setLayoutY(107);
		btn_StartStop.setPrefWidth(170);
		btn_StartStop.setId("btn-StartStop");
		mainLayoutPane.getChildren().add(btn_StartStop);
		btn_StartStop.addEventHandler(MouseEvent.MOUSE_CLICKED,
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent e) {
          	if(!isRecording){
	          	startRecording();
	          	btn_StartStop.setText("STOP");
	          }
	          else
	          {
	          	stopRecording();
	          	btn_StartStop.setText("START");
	          }
	          isRecording = !isRecording;
	      }
        });




		// Set up and layout our min window
		minLayoutPane = new Pane();
		minLayoutPane.getStylesheets().add("application.css");
		minScene = new Scene(minLayoutPane, 8, 8);

		btn_Maximize = new Button(" ");
		btn_Maximize.setId("btn-Maximize");
		minLayoutPane.getChildren().add(btn_Maximize);

		btn_Maximize.addEventHandler(MouseEvent.MOUSE_CLICKED,
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent e) {
			goMaximized();
	      }
        });

		mainStage.show();
		//goMaximized();
		goMinimized();

    }

	public static void main(String[] args) {
		launch(args);
	}





	public void goMinimized(){
		mainStage.setScene(minScene);
		setTopRight();
	}


	public void goMaximized(){
		mainStage.setScene(mainScene);
		setTopRight();
	}


	public void startRecording(){
		btn_Minimize.setStyle("-fx-background-color: #f00;");
		btn_Maximize.setStyle("-fx-background-color: #f00;");

		btn_StartStop.setStyle("-fx-border-color: #f00;-fx-text-fill: #f00;");
		btn_ViewFolder.setStyle("-fx-border-color: #f00;-fx-text-fill: #f00;");
		btn_Refresh.setStyle("-fx-border-color: #f00;-fx-text-fill: #f00;");
		projectSelector.setStyle("-fx-border-color: #f00;-fx-text-fill: #f00;");
		txt_notes.setStyle("-fx-border-color: #f00;-fx-text-fill: #f00;");


		goMinimized();
		btn_Refresh.setDisable(true);
		projectSelector.setDisable(true);
		txt_notes.setDisable(true);
		startTime = "" + (System.currentTimeMillis() / 1000);
	}


	public void stopRecording(){

		try{
			String endProject = (String) projectSelector.getValue();
			endProject = endProject.replaceAll(",","");
			String endTime = "" + (System.currentTimeMillis() / 1000);
			String endText = "" + txt_notes.getText().replaceAll(",","");
			String logTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

			FileWriter fw = new FileWriter("data/output.csv", true);
 			fw.write(logTime + "," + startTime + "," + endTime + "," + endProject + "," + endText + "\n");
 			fw.close();
	 	}
 		catch(Exception e){}



		btn_Minimize.setStyle("");
		btn_Maximize.setStyle("");
		btn_StartStop.setStyle("");
		btn_ViewFolder.setStyle("");
		btn_Refresh.setStyle("");
		projectSelector.setStyle("");
		txt_notes.setStyle("");

		btn_Refresh.setDisable(false);
		projectSelector.setDisable(false);
		txt_notes.setDisable(false);
	}


	public void loadProjectList(){
		try{

			projectSelector.getItems().clear();
			InputStream input = TimeSheeter.class.getClassLoader().getResourceAsStream("data/projects.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			while((line = br.readLine()) != null)
				projectSelector.getItems().add(line);
			projectSelector.getSelectionModel().selectFirst();

			br.close();
			input.close();

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	public void setTopRight(){
		List<Screen> screens = Screen.getScreens();
		int currentScreen = 0;
		int showOnScreen = 0;
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		// Go through each screen 
		for (Screen screen : screens) {
			if(currentScreen == showOnScreen)
			{
				screenBounds = screen.getVisualBounds();
            }
            currentScreen++;
        }
		mainStage.setX(screenBounds.getMinX() + screenBounds.getWidth() - mainStage.getWidth());
		mainStage.setY(0);

		mainStage.toFront();
		mainStage.show();

	}
}