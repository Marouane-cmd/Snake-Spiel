import javax.print.DocFlavor.URL;
import javax.swing.plaf.basic.BasicComboBoxUI.KeyHandler;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SnakeGame extends Application{


	// Variabeln

	public enum Direction {

		UP , DOWN , RIGHT , LEFT ;
	}

	// Fenster 

	private static Stage window;

	public final static int BLOCK_SIZE = 20;
	public final static int GAME_WIDTH = 30 * BLOCK_SIZE;
	public final static int GAME_HEIGHT = 20*BLOCK_SIZE;

	private static double speed = 0.2;
	private static boolean isEndless= false;

	private Direction direction = Direction.RIGHT;
	private boolean moved = false ;
	private boolean running = false ;

	private Timeline timeline = new Timeline();

	private ObservableList<Node> snake;

	private MediaPlayer mediaPlayer ;
	private Slider volumeSlider = new Slider() ;
	private Label volumeLabel = new Label("1.0");

	private int score = 0;
	private Label scoreLbel = new Label("SCORE : " + score);
	private Label infoLabel = new Label("Drücke ESC für Exit und SPACE für Pause !");

	// Gameszene
	private Pane crateGameContent () {
		Pane root = new Pane();

		root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
		root.setStyle(
				"-fx-background-image: url(images/tracer.png);" 
						+ "-fx-background-size: 20 20;"
						+ "-fx-background-repeat: repeat;"
						+ "-fx-border-color: black;"
						+ "-fx-border-style: solid;"
						+ "-fx-border-width: 2;");

		// Schlange
		Group snakeBody = new Group();
		snake = snakeBody.getChildren();

		// Essen 
		Rectangle food = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
		Image foodImage = new Image("images/food.png");
		ImagePattern imagePattern= new ImagePattern(foodImage);
		food.setFill(imagePattern);

		createRandomFood(food);



		// Animation
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(speed), new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (!running) {
					return;
				}
				boolean toRemove = snake.size()>1;
				//
				Node tail ;  // Kopf ..bzw Ende der Schlange
				if(toRemove) {
					tail= snake.remove(snake.size()-1);
				}else {
					tail = snake.get(0);
				}

				double tailX = tail.getTranslateX();
				double tailY = tail.getTranslateY();
				switch(direction) {

				case UP:
					tail.setTranslateX(snake.get(0).getTranslateX());
					tail.setTranslateY(snake.get(0).getTranslateY()-BLOCK_SIZE);
					break;

				case DOWN:	
					tail.setTranslateX(snake.get(0).getTranslateX());
					tail.setTranslateY(snake.get(0).getTranslateY()+BLOCK_SIZE);
					break;

				case LEFT:	
					tail.setTranslateX(snake.get(0).getTranslateX()-BLOCK_SIZE);
					tail.setTranslateY(snake.get(0).getTranslateY());
					break;

				case RIGHT:	
					tail.setTranslateX(snake.get(0).getTranslateX()+BLOCK_SIZE);
					tail.setTranslateY(snake.get(0).getTranslateY());
				default:
					break;

				}
				moved = true;
				if(toRemove) {
					snake.add(0,tail);
				}

				// Kollision
				for (Node rect:snake ) {
					if(rect != tail && tail.getTranslateX()==rect.getTranslateX() && tail.getTranslateY()==rect.getTranslateY()  ) {
						score= 0;
						scoreLbel.setText("Score: " + score);
						restartGame();
						break;
					}
				}
				// Wand oder nicht ?
				if(isEndless) {
					gameIsEndless(tail, root);
				}
				else {
					gameIsNoEndless(tail, food);
				}

				// Food einsammeln
				if(tail.getTranslateX()==food.getTranslateX() && tail.getTranslateY()== food.getTranslateY()) {
					createRandomFood(food);
					score+=20;
					scoreLbel.setText("Score: " + score);

					Rectangle rectangle = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
					rectangle.setTranslateX(tailX);
					rectangle.setTranslateY(tailY);
					snake.add(rectangle);


				}
			}
		});

		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Timeline.INDEFINITE);


		// ScoreLbel

		scoreLbel.setFont(Font.font("Arial", 30));
		scoreLbel.setTranslateX(GAME_WIDTH/2);

		// InfoLabel
		infoLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 10));

		root.getChildren().addAll(food,snakeBody,scoreLbel,infoLabel);

		return root;

	}
	// Random food spawn
	private void createRandomFood(Node food) {

		food.setTranslateX((int)( Math.random() *(GAME_WIDTH-BLOCK_SIZE))/BLOCK_SIZE*BLOCK_SIZE);
		food.setTranslateY((int)( Math.random() *(GAME_HEIGHT-BLOCK_SIZE))/BLOCK_SIZE*BLOCK_SIZE);
	}
	// If IsEndless
	private void gameIsEndless(Node tail , Parent root) {
		root.setStyle(
				"-fx-background-image: url(images/tracer.png);" 
						+ "-fx-background-size: 20 20;"
						+ "-fx-background-repeat: repeat;"
				);
		if(tail.getTranslateX()<0) {
			tail.setTranslateX((GAME_WIDTH-BLOCK_SIZE));
		}
		if(tail.getTranslateX()>=GAME_WIDTH) {
			tail.setTranslateX(0);
		}
		if(tail.getTranslateY()<0) {
			tail.setTranslateY((GAME_HEIGHT-BLOCK_SIZE));
		}
		if(tail.getTranslateY()>=GAME_HEIGHT) {
			tail.setTranslateY(0);
		}
	}
	// If IsNoEndless

	private void gameIsNoEndless (Node tail , Node food) {
		if(tail.getTranslateX()<0 || tail.getTranslateX()>=GAME_WIDTH || tail.getTranslateY()<0 || tail.getTranslateY()>=GAME_HEIGHT) {
			score= 0 ;
			scoreLbel.setText("Score: " + score);
			restartGame();
			createRandomFood(food);
		}
	}

	// Start Game 
	private void startGame() {
		Rectangle head = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
		snake.add(head);

		timeline.play();
		running= true;
	}
	// RestartGame

	private void restartGame() {
     stopGame();
     startGame();
	}

	// Stop
	private void stopGame() {
		running=false;
		timeline.stop();
		snake.clear();
	}
	
	private BorderPane createStartScree() {
		BorderPane root  = new BorderPane();

		// START 
		Label startLabel = new Label();
		Image image = new Image (getClass().getResourceAsStream("images/snake.png"));
		ImageView imageView = new ImageView(image);
		startLabel.setGraphic(imageView);

		Button startButton = new Button ("START");
		startButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				//System.out.println("SpielStart");
				Scene scene = new Scene(crateGameContent());
				keypressed(scene);
				
				window.setScene(scene);
				window.setResizable(false);
				window.setTitle("SNAKE GAME");
				window.show();

               startGame();
			}
		});

		VBox vbox = new VBox(30);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(startLabel, startButton);
		root.setTop(vbox);

		// Exit 

		Button exitButton = new Button("Exit");
		BorderPane.setAlignment(exitButton, Pos.CENTER);
		BorderPane.setMargin(exitButton, new Insets(20));
		root.setBottom(exitButton);

		exitButton.setOnAction(new EventHandler<ActionEvent >() {

			@Override
			public void handle(ActionEvent event) {
				Platform.exit();
			}
		});

		// Einstellung

		Button speedButton = new Button("Speed");
		Button endlessOrNot = new Button("Rand ✔");
		Label speedLabel = new Label ("Leicht");

		speedButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (speed== 0.2) {
					SnakeGame.speed=1.5;
					speedLabel.setText("Mittel");
				}else if(speed==1.5) {
					SnakeGame.speed= 0.09;
					speedLabel.setText("Schwer");
				}else if (speed==0.09) {
					SnakeGame.speed= 0.2;
					speedLabel.setText("Leicht");
				}
			}
		});

		endlessOrNot.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if(isEndless) {
					endlessOrNot.setText("Rand ✔");
					isEndless= false;
				}
				else { 
					endlessOrNot.setText("Rand  ❌");
					isEndless= true;
				}}
		});
		HBox hBox = new HBox(10);
		hBox.setAlignment(Pos.CENTER);
		hBox.getChildren().addAll(speedButton,speedLabel,endlessOrNot);
		root.setCenter(hBox);

		// Musik

		Button muteButton= new Button("",new ImageView(new Image(getClass().getResourceAsStream("images/mute.png"))));
		Button unmuteButton= new Button("",new ImageView(new Image(getClass().getResourceAsStream("images/unmute.png"))));

		muteButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				mediaPlayer.pause();	
			}
		});
		unmuteButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				mediaPlayer.play();	

			}
		});


		HBox hBox2= new HBox(5);
		hBox2.getChildren().addAll(volumeSlider,volumeLabel);
		VBox vBox2 = new VBox(5);
		vBox2.setAlignment(Pos.CENTER_RIGHT);
		vBox2.getChildren().addAll(unmuteButton,muteButton, new Separator(),hBox2);

		root.setRight(vBox2);
		BorderPane.setMargin(vBox2, new Insets(20));

		return root;
	}
	// Musik 
	private void playMusic(String title) {
		String musicFile = title;
		java.net.URL fileUrl = getClass().getResource(musicFile);

		Media media= new Media(fileUrl.toString());
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);


	}
	// Tastatur Interaktion
	private void keypressed(Scene scene) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
			//System.out.println("der Nutzer hat diese Teste ausgedrückt ...: " + event.getCode());	
				if(!moved) {
					return;
				}
				switch (event.getCode()) {
				case W:
				case UP:
					if(direction != Direction.DOWN ) {
						direction = Direction.UP;
						break;
					}
					
				case S:
				case DOWN:
					if(direction != Direction.UP ) {
						direction = Direction.DOWN;
						break;
					}
				case F:
				case RIGHT:
					if(direction != Direction.LEFT ) {
						direction = Direction.RIGHT;
						break;
					}
				case A:
				case LEFT:
					if(direction != Direction.RIGHT ) {
						direction = Direction.LEFT;
						break;
					}
					
				case SPACE:
					timeline.pause();
					scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

						@Override
						public void handle(KeyEvent event) {
							if(event.getCode()==KeyCode.SPACE) {
								timeline.playFromStart();
								keypressed(scene);
							}else if (event.getCode()==KeyCode.ESCAPE) {
								Platform.exit();
							}
						}
					});
					break;
				case ESCAPE:
					Platform.exit();
					break;

				default:
					break;
				}
				moved= false;
			}
		});
	}


	@Override
	public void init() throws Exception {
		String musicFile = "music/snakeMusic.mp3";
		/* playMusic(musicFile);

      volumeSlider.setValue(mediaPlayer.getVolume()*100);
      volumeSlider.setPrefWidth(80);
      volumeSlider.setShowTickLabels(true);*/
	}


	@Override
	public void start(Stage primaryStage) throws Exception {

		Parent root= createStartScree();
		primaryStage.setResizable(false);
		primaryStage.setTitle("Snake");
		window = primaryStage;
		window.setScene(new Scene(root,GAME_WIDTH,GAME_HEIGHT) );
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);

	}

}
