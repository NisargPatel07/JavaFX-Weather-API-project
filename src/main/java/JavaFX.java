// JavaFX core imports for building the application
import javafx.application.Application;  // Base class for JavaFX applications

// Layout management imports
import javafx.geometry.Insets;    // Handles spacing around elements (padding/margins)
import javafx.geometry.Pos;       // Controls alignment of elements in containers

// UI component imports
import javafx.scene.Scene;        // Container for all visible content
import javafx.scene.control.Button;  // Creates interactive buttons
import javafx.scene.control.Label;   // Displays text labels
import javafx.scene.control.ScrollPane;  // Makes content scrollable

// Image handling for weather icons
import javafx.scene.image.Image;     // Loads image files
import javafx.scene.image.ImageView; // Displays images in the UI

// Layout container imports
import javafx.scene.layout.BorderPane;  // Main layout dividing space into regions
import javafx.scene.layout.HBox;        // Arranges elements horizontally
import javafx.scene.layout.VBox;        // Arranges elements vertically

// Visual styling imports
import javafx.scene.paint.Color;        // Basic color definitions
import javafx.scene.shape.Rectangle;        // Draws rectangular shapes

// Text styling imports
import javafx.scene.text.Font;         // For font manipulation
import javafx.scene.text.FontWeight;   // For font weight (bold, etc.)
import javafx.scene.text.TextAlignment; // For text alignment

// Stage (window) management
import javafx.stage.Stage;             // Main application window

// Custom weather classes
import weather.Period;                 // Weather period data structure
import weather.WeatherAPI;             // Weather API interface

// Java utilities
import java.io.InputStream;            // For reading resource files (images)
import java.util.ArrayList;            // For storing weather forecast periods
import java.util.HashMap;              // For city data mapping
import java.util.Map;                  // For city data interface

/**
 * Main JavaFX Application class
 * Extends Application to create the weather forecast GUI
 */
public class JavaFX extends Application {
  // Stores weather forecast data retrieved from API
  private ArrayList<Period> forecast;

  // Main window of the application
  private Stage primaryStage;

  // Different scenes for navigation
  private Scene welcomeScene;      // Initial greeting screen
  private Scene citySelectionScene;// City selection screen
  private Scene todayScene;        // Today's weather display
  private Scene forecastScene;     // 7-day forecast display

  // Maps city names to their API grid coordinates
  private Map<String, int[]> cityData = new HashMap<>();

  // Current selected city information
  private String currentCity = "Chicago, IL";  // Default city
  private String currentRegion = "LOT";        // Weather region code
  private int currentGridX = 77;               // Grid X coordinate
  private int currentGridY = 70;               // Grid Y coordinate

  // Weather image mapping
  // map the icon based on the shortfoast returned from the API
  // diffrent condition along with designated icons to show real time GIF
  // it also has a conditions to change the icon if the condition is returned and checks if the time is night then show different GIF
  // checks if isNight then show night theme GIf instead of day time
  private String getWeatherIconPath(String description, boolean isNight) {
    String iconPath = "/icons/default.png";
    description = description.toLowerCase();

    description = description.toLowerCase();
    if (description.contains("showers") && description.contains("thunderstorm")) {
      iconPath = "/icons/thunder.gif";
    } else if (description.contains("sunny") || description.contains("clear")) {
      if (isNight) {
        iconPath = "/icons/clearnight.gif"; // Use clear night icon instead of sunny for night
      } else {
        iconPath = "/icons/sunnysky.gif";
      }
    } else if (description.contains("Mostly Cloudy") || description.contains("cloud") || description.contains("partly")) {
      if (isNight) {
        iconPath = "/icons/mostlyclear.gif";
      } else {
        iconPath = "/icons/clouldysky.gif";
      }
    } else if (description.contains("rain") || description.contains("shower") ||
        (description.contains("chance") && description.contains("rain"))) {
      iconPath = "/icons/rain.gif";
    } else if (description.contains("snow")) {
      iconPath = "/icons/snow.gif";
    } else if (description.contains("wind")) {
      iconPath = "/icons/wind.gif";
    } else if (description.contains("fog") || description.contains("patchy")) {
      if (isNight) {
        iconPath = "/icons/cloudy.gif";
      } else {
        iconPath = "/icons/clouldysky.gif";
      }
    } else if (description.contains("smoke")) {
      iconPath = "/icons/foggy.gif";
    }

    return iconPath;
  }

  //runs the applications
  public static void main(String[] args) {
    launch(args);
  }

  //start the application and show the main welcome screen
  //creates different scene handles for all 4 scene and get data from the API
  //

  // when we run the code this create the welcome screen
  @Override
  public void start(Stage primaryStage) throws Exception {
    this.primaryStage = primaryStage;
    primaryStage.setTitle("Weather Forecast App");

    setupCityData(); // add cities
    createWelcomeScene(); // make welcome page
    createCitySelectionScene(); // make city pick page
    loadForecast(); // get forecast for default city

    // Set initial scene
    primaryStage.setScene(welcomeScene); // show welcome screen
    primaryStage.show();
  }

  /**
   * City Data Configuration
   * Purpose: this adds city names and their grid data
   * Process:
   * - Stores city information in HashMap
   * - Each city mapped to array containing:
   *   > Weather office code (3 chars)
   *   > Grid X coordinate
   *   > Grid Y coordinate
   * Location: Called during application startup in start() method
   */
  private void setupCityData() {
    // Format: City name => {region code, grid X, grid Y}
    cityData.put("Chicago, IL", new int[]{'L', 'O', 'T', 77, 70});
    cityData.put("New York, NY", new int[]{'O', 'K', 'X', 40, 50});
    cityData.put("Los Angeles, CA", new int[]{'L', 'O', 'X', 155, 44});
    cityData.put("Miami, FL", new int[]{'M', 'F', 'L', 110, 50});
    cityData.put("Seattle, WA", new int[]{'S', 'E', 'W', 130, 67});
  }

  /**
   * Forecast Loading Handler
   * Purpose: this gets weather data and builds scenes
   * Process:
   * - Makes API call using current city's grid coordinates
   * - Creates new scenes with updated forecast data
   * - Handles errors with user-friendly error display
   * Location: Called after city selection and during initial load
   * Error Handling:
   * - Shows error scene if API call fails
   * - Provides option to return to city selection
   */
  private void loadForecast() {

    // try to get forecast data from the API using current city info
    try {
      // get the following information from the API
      forecast = WeatherAPI.getForecast(currentRegion, currentGridX, currentGridY);

      // if there is no forecast error message pop-ups
      if (forecast == null) {
        throw new RuntimeException("Forecast did not load");
      }

      // build the screens for today and 7-day forecast
      createTodayScene();
      createForecastScene();
    } catch (Exception e) { // if something goes wrong, print the error
      e.printStackTrace();
      // Show error message if forecast fails to load just in case of no connection to API
      Label errorLabel = new Label("Unable to load forecast data. Please try again.");
      errorLabel.setFont(Font.font("Verdana", 16));
      errorLabel.setTextFill(Color.RED);
      // give user back button incase they can't see the data 
      Button backButton = new Button("Back to City Selection");
      backButton.setOnAction(event -> primaryStage.setScene(citySelectionScene));

      // arrange error message and button vertically
      VBox errorBox = new VBox(20, errorLabel, backButton);
      errorBox.setAlignment(Pos.CENTER);
      errorBox.setPadding(new Insets(50));
      errorBox.setStyle("-fx-background-color: white;");

      // create new scene to show the error
      Scene errorScene = new Scene(errorBox, 1200, 700);
      primaryStage.setScene(errorScene);
    }
  }

  /**
   * Welcome Scene Creator
   * Purpose: Creates the initial welcome screen
   * Process:
   * - Sets up layout with title, description, and start button
   * - Loads a welcome image if available
   * - Handles potential image loading errors gracefully
   * Location: Called during application startup in start() method
   * Visual Elements:
   * - Title label with large font size and weight
   * - Description label with smaller font size
   * - Start button with styling and action
   * - Optional welcome image
   */
  private void createWelcomeScene() {
    // Main container
    VBox welcomeContainer = new VBox(30);
    welcomeContainer.setAlignment(Pos.CENTER);
    welcomeContainer.setPadding(new Insets(40));
    welcomeContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #90caf9, #1a237e);");


    // App title
    Label titleLabel = new Label("Weather Forecast App");
    titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 36));
    titleLabel.setTextFill(Color.WHITE);

    // App description label
    Label descriptionLabel = new Label("Get accurate weather forecasts for cities across the United States");
    descriptionLabel.setFont(Font.font("Verdana", 18));
    descriptionLabel.setTextFill(Color.WHITE);

    // Try to load and display weather icon
    try {
      String iconPath = "/icons/logo.png";
      InputStream is = getClass().getResourceAsStream(iconPath);
      if (is != null) {
        Image welcomeImage = new Image(is);
        ImageView welcomeIcon = new ImageView(welcomeImage);
        welcomeIcon.setFitHeight(200);
        welcomeIcon.setFitWidth(200);
        welcomeContainer.getChildren().add(welcomeIcon);
      }
    } catch (Exception e) {
      // do nothing if image fails to load
    }

    // button to go to next screen
    Button enterButton = new Button("Get Started");
    enterButton.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
    enterButton.setPadding(new Insets(10, 30, 10, 30));
    enterButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    enterButton.setOnAction(e -> primaryStage.setScene(citySelectionScene));

    // Copyright info
    Label copyrightLabel = new Label("© 2025 Weather App - CS342 Project");
    copyrightLabel.setFont(Font.font("Verdana", 12));
    copyrightLabel.setTextFill(Color.LIGHTGRAY);

    // Add all elements to the container
    welcomeContainer.getChildren().addAll(titleLabel, descriptionLabel, enterButton, copyrightLabel);

    // Create scene
    welcomeScene = new Scene(welcomeContainer, 1200, 700);
  }

  /**
   * City Selection Scene Creator
   * Purpose: Creates the city selection interface
   * Process:
   * - Creates grid of buttons for each available city
   * - Each button updates current city and loads its forecast
   * - Provides visual feedback on current selection
   * Location: Called after welcome scene
   * Data Handling:
   * - Uses cityData map to get coordinates for API calls
   * - Updates currentCity, currentRegion, and grid coordinates
   */
  private void createCitySelectionScene() {
    // base layout for the whole screen
    BorderPane mainLayout = new BorderPane();
    mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #90caf9, #1a237e);");


    // create the title at the top
    Label titleLabel = new Label("Select a City");
    titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
    titleLabel.setTextFill(Color.WHITE);

    // put title in a horizontal box
    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(15));
    header.getChildren().add(titleLabel);
    header.setStyle("-fx-background-color: #2c3e50;");
    mainLayout.setTop(header);

    // vertical box for everything in the middle
    VBox contentBox = new VBox(30);
    contentBox.setAlignment(Pos.CENTER);
    contentBox.setPadding(new Insets(40));

    // vertical box for city image and buttons
    VBox cityButtonsContainer = new VBox(15);
    cityButtonsContainer.setAlignment(Pos.CENTER);

    // try to show an animated city gif at the top
    try {
      InputStream is = getClass().getResourceAsStream("/icons/city.gif");
      if (is != null) {
        Image cityGif = new Image(is);
        ImageView cityIcon = new ImageView(cityGif);
        cityIcon.setFitHeight(100);
        cityIcon.setFitWidth(150);
        cityButtonsContainer.getChildren().add(cityIcon);
      }
    } catch (Exception e) {
      // do nothing if image fails to load
    }

    // show instructions to the user
    Label instructionsLabel = new Label("Select a city to view its weather forecast");
    instructionsLabel.setFont(Font.font("Verdana", 16));
    cityButtonsContainer.getChildren().add(instructionsLabel);

    // create one button for each city
    for (String cityName : cityData.keySet()) {
      Button cityButton = new Button(cityName);
      cityButton.setPrefWidth(250);
      cityButton.setPrefHeight(50);
      cityButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px;");

      // change style when mouse hovers over the button
      cityButton.setOnMouseEntered(e ->
          cityButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 16px;"));
      cityButton.setOnMouseExited(e ->
          cityButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px;"));

      // when user clicks a city, update city info and load weather
      cityButton.setOnAction(e -> {
        currentCity = cityName;
        int[] cityCoords = cityData.get(currentCity);
        if (cityCoords.length >= 5) {
          currentRegion = String.valueOf((char)cityCoords[0]) +
              String.valueOf((char)cityCoords[1]) +
              String.valueOf((char)cityCoords[2]);
          currentGridX = cityCoords[3];
          currentGridY = cityCoords[4];
        }

        loadForecast(); // get new weather info
        primaryStage.setScene(todayScene); // go to today's forecast scene
      });

      cityButtonsContainer.getChildren().add(cityButton); // add button to list
    }

    // back button to go to welcome screen
    Button backButton = new Button("Back to Welcome");
    backButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
    backButton.setPadding(new Insets(10, 20, 10, 20));
    backButton.setOnAction(e -> primaryStage.setScene(welcomeScene));

    // add all city buttons and back button to middle box
    contentBox.getChildren().addAll(
        cityButtonsContainer,
        backButton
    );

    mainLayout.setCenter(contentBox);

    // build the full scene
    citySelectionScene = new Scene(mainLayout, 1200, 700);
  }

  /**
   * Today's Weather Scene Creator
   * Purpose:  this builds the screen where users choose a city
   * Process:
   * - Creates layout with current conditions
   * - Shows temperature, description, and weather icon
   * - Displays day and night forecast cards
   * Location: Called after forecast data is loaded
   * Visual Elements:
   * - Weather icons based on conditions
   * - Temperature display with styling
   * - Navigation buttons to other scenes
   */
  private void createTodayScene() {
    // base layout for the whole screen
    Period today = forecast.get(0);

    // create the title at the top
    Label titleLabel = new Label("Weather Today");
    titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
    titleLabel.setTextFill(Color.WHITE);

    // put title in a horizontal box
    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(15));
    header.getChildren().add(titleLabel);
    header.setStyle("-fx-background-color: #2c3e50;");

    // button to go to 7-day forecast scene
    Button forecastButton = new Button("7-Day Forecast");
    forecastButton.setOnAction(e -> primaryStage.setScene(forecastScene));
    forecastButton.setStyle("-fx-background-color: #4287f5; -fx-text-fill: white;");
    forecastButton.setPrefWidth(150);
    forecastButton.setPrefHeight(30);

    // button to go back to city selection scene
    Button cityButton = new Button("Change City");
    cityButton.setOnAction(e -> primaryStage.setScene(citySelectionScene));
    cityButton.setStyle("-fx-background-color: #f54242; -fx-text-fill: white;");
    cityButton.setPrefWidth(150);
    cityButton.setPrefHeight(30);

    // container for bottom navigation buttons
    HBox bottomNav = new HBox(20);
    bottomNav.setAlignment(Pos.CENTER);
    bottomNav.setPadding(new Insets(15));
    bottomNav.getChildren().addAll(cityButton, forecastButton);



    // add title and detailed forecast for current city
    Label detailTitle = new Label("What to Expect Today in " + currentCity);
    detailTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
    detailTitle.setTextFill(Color.WHITE);
    detailTitle.setAlignment(Pos.CENTER);

    // shows the full text forecast for the day
    Label detailText = new Label(today.detailedForecast);
    detailText.setFont(Font.font("Verdana", 14));
    detailText.setTextFill(Color.WHITE);
    detailText.setWrapText(true);
    detailText.setMaxWidth(600);
    detailText.setAlignment(Pos.CENTER);
    detailText.setTextAlignment(TextAlignment.CENTER);

    // vertical box to hold the location forecast and description
    VBox locationInfo = new VBox(10);
    locationInfo.setAlignment(Pos.CENTER);
    locationInfo.getChildren().addAll( detailTitle, detailText);

    // container to hold both day and night weather cards side by side
    HBox weatherContainer = new HBox(30);
    weatherContainer.setAlignment(Pos.CENTER);
    weatherContainer.setPadding(new Insets(30));

    // Day forecast
    Period dayPeriod = forecast.get(0); // today's daytime weather
    VBox dayDisplay = new VBox(15);
    dayDisplay.setAlignment(Pos.CENTER);
    dayDisplay.setPadding(new Insets(20));
    dayDisplay.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 15;");
    dayDisplay.setPrefWidth(400);

    // label for day time period name (like “Monday”)
    Label dayLabel = new Label(dayPeriod.name+" ☀");
    dayLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
    dayLabel.setTextFill(Color.WHITE);

    // label showing temperature
    Label dayTemp = new Label(dayPeriod.temperature + "°" + dayPeriod.temperatureUnit);
    dayTemp.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
    dayTemp.setTextFill(Color.WHITE);

    // short summary of the weather
    Label dayDesc = new Label(dayPeriod.shortForecast);
    dayDesc.setFont(Font.font("Verdana", 16));
    dayDesc.setTextFill(Color.WHITE);
    dayDesc.setWrapText(true);
    dayDesc.setTextAlignment(TextAlignment.CENTER);

    // try to add a weather icon image for day time
    try {
      String iconPath = getWeatherIconPath(dayPeriod.shortForecast, false);
      InputStream is = getClass().getResourceAsStream(iconPath);
      if (is != null) {
        Image weatherImage = new Image(is);
        ImageView dayIcon = new ImageView(weatherImage);
        dayIcon.setFitHeight(100);
        dayIcon.setFitWidth(100);
        dayDisplay.getChildren().addAll(dayLabel, dayIcon, dayTemp, dayDesc);
      }
    } catch (Exception e) {
      // if icon can’t load, still show text info
      dayDisplay.getChildren().addAll(dayLabel, dayTemp, dayDesc);
    }

    // Night forecast
    Period nightPeriod = forecast.get(1); // tonight’s weather info
    VBox nightDisplay = new VBox(15);
    nightDisplay.setAlignment(Pos.CENTER);
    nightDisplay.setPadding(new Insets(20));
    nightDisplay.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 15;");
    nightDisplay.setPrefWidth(400);

    // label for night section
    Label nightLabel = new Label("Night 🌙");
    nightLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
    nightLabel.setTextFill(Color.WHITE);

    // temperature at night
    Label nightTemp = new Label(nightPeriod.temperature + "°" + nightPeriod.temperatureUnit);
    nightTemp.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
    nightTemp.setTextFill(Color.WHITE);

    // short weather description
    Label nightDesc = new Label(nightPeriod.shortForecast);
    nightDesc.setFont(Font.font("Verdana", 16));
    nightDesc.setTextFill(Color.WHITE);
    nightDesc.setWrapText(true);
    nightDesc.setTextAlignment(TextAlignment.CENTER);

    // try to load weather icon for night
    try {
      String iconPath = getWeatherIconPath(nightPeriod.shortForecast, true);
      InputStream is = getClass().getResourceAsStream(iconPath);
      if (is != null) {
        Image weatherImage = new Image(is);
        ImageView nightIcon = new ImageView(weatherImage);
        nightIcon.setFitHeight(100);
        nightIcon.setFitWidth(100);
        nightDisplay.getChildren().addAll(nightLabel, nightIcon, nightTemp, nightDesc);
      }
    } catch (Exception e) {
      // if icon can’t load, still show text info
      nightDisplay.getChildren().addAll(nightLabel, nightTemp, nightDesc);
    }

    // thin line between day and night boxes
    Rectangle divider = new Rectangle(2, 300);
    divider.setFill(Color.WHITE);
    divider.setOpacity(0.3);

    // add both day and night forecast boxes with divider
    weatherContainer.getChildren().addAll(dayDisplay, divider, nightDisplay);

    // combine forecast description and the cards
    VBox weatherDisplay = new VBox(20);
    weatherDisplay.setAlignment(Pos.CENTER);
    weatherDisplay.getChildren().addAll(locationInfo, weatherContainer);

    // Additional details
    HBox detailsBox = new HBox(40);
    detailsBox.setAlignment(Pos.CENTER);



    // box showing wind speed and direction
    VBox windInfo = new VBox(5);
    windInfo.setAlignment(Pos.CENTER);
    Label windLabel = new Label("Wind");
    windLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
    windLabel.setTextFill(Color.WHITE);
    Label windValue = new Label(today.windSpeed + " " + today.windDirection);
    windValue.setTextFill(Color.WHITE);
    windInfo.getChildren().addAll(windLabel, windValue);

    // box showing chance of rain
    VBox precipInfo = new VBox(5);
    precipInfo.setAlignment(Pos.CENTER);
    Label precipLabel = new Label("Precipitation");
    precipLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
    precipLabel.setTextFill(Color.WHITE);
    int precipValue = 0;
    if (today.probabilityOfPrecipitation != null && today.probabilityOfPrecipitation.value > 0) {
      precipValue = today.probabilityOfPrecipitation.value;
    }
    Label precipValueLabel = new Label(precipValue + "%");
    precipValueLabel.setTextFill(Color.WHITE);
    precipInfo.getChildren().addAll(precipLabel, precipValueLabel);

    detailsBox.getChildren().addAll(windInfo, precipInfo); // add wind and rain info to the row



    // build main layout using BorderPane
    BorderPane mainLayout = new BorderPane();
    mainLayout.setTop(header);

    // center contains forecast details and weather cards
    VBox centerContent = new VBox(20);
    centerContent.setAlignment(Pos.CENTER);
    centerContent.getChildren().addAll(weatherDisplay, detailsBox);
    mainLayout.setCenter(centerContent);
    mainLayout.setBottom(bottomNav); // navigation buttons at the bottom

    // Create scene with light blue background
    todayScene = new Scene(mainLayout, 1200, 700);
    mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #90caf9, #1a237e);");
  }

  /**
   * 7-Day Forecast Scene Creator
   * Purpose: Creates the scene for displaying the 7-day forecast
   * Process:
   * - Creates a scrollable horizontal layout to display forecast cards
   * - Each card shows the day's weather information
   * - Uses images for weather icons
   * Location: Called after forecast data is loaded
   * Visual Elements:
   * - Scrollable horizontal layout for viewing forecast cards
   * - Cards with day/night information and weather icons
   * - Navigation buttons to other scenes
   */
  private void createForecastScene() {

    // create top label that says "7-Day Forecast"
    Label titleLabel = new Label("7-Day Forecast");
    titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
    titleLabel.setTextFill(Color.WHITE);

    // put the title in a horizontal box at the top
    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(15));
    header.getChildren().add(titleLabel);
    header.setStyle("-fx-background-color: #2c3e50;");

    // button to go to today's weather
    Button todayButton = new Button("Today's Weather");
    todayButton.setOnAction(e -> primaryStage.setScene(todayScene));
    todayButton.setStyle("-fx-background-color: #4287f5; -fx-text-fill: white;");
    todayButton.setPrefWidth(150);
    todayButton.setPrefHeight(30);

    // button to go back to city selection
    Button cityButton = new Button("Change City");
    cityButton.setOnAction(e -> primaryStage.setScene(citySelectionScene));
    cityButton.setStyle("-fx-background-color: #f54242; -fx-text-fill: white;");
    cityButton.setPrefWidth(150);
    cityButton.setPrefHeight(30);

    // navigation buttons container at the bottom
    HBox bottomNav = new HBox(20);
    bottomNav.setAlignment(Pos.CENTER);
    bottomNav.setPadding(new Insets(15));
    bottomNav.getChildren().addAll(cityButton, todayButton);

    // show the selected city above the forecast cards
    Label locationLabel = new Label("For " + currentCity);
    locationLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
    locationLabel.setPadding(new Insets(10, 0, 0, 0));

    // scrollable area for forecast cards
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setPannable(true);
    scrollPane.setFitToHeight(true);

    // horizontal container to hold forecast cards
    HBox forecastContainer = new HBox(15);
    forecastContainer.setAlignment(Pos.CENTER_LEFT);
    forecastContainer.setPadding(new Insets(20));
    forecastContainer.setSpacing(15);

    // Process the forecast data to group by day
    int day = 0;
    int maxDays = 12;

    // Process forecast periods properly matching day/night
    int i = 0;

    // go through forecast and build cards for day/night pairs
    while (i < forecast.size() - 1 && day < maxDays) {
      if (day >= maxDays) break;

      // Get day and night periods if available
      Period dayPeriod = (i < forecast.size()) ? forecast.get(i) : null;
      Period nightPeriod = (i+1 < forecast.size()) ? forecast.get(i+1) : null;


      if (dayPeriod.isDaytime && !nightPeriod.isDaytime) {

        i += 2;
        day++;
      } else {
        i++;
      }

      // Skip if no day period (shouldn't happen but just in case)
      if (dayPeriod == null) continue;

      // Skip if both are null (shouldn't happen)
      if (dayPeriod == null && nightPeriod == null) continue;

      // Create a card for this day
      VBox card = new VBox(5);
      card.setAlignment(Pos.CENTER);
      card.setPadding(new Insets(15));
      card.setStyle("-fx-background-image: url('/icons/cardback.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 10);");
      card.setPrefWidth(250);


      String dayName;
      if (i == 1) {
        dayName = "Today";
      } else {
        dayName = dayPeriod != null ? dayPeriod.name :
            (nightPeriod != null ? nightPeriod.name : "Unknown");
      }

      Label dateLabel = new Label(dayName);
      dateLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
      dateLabel.setTextFill(Color.web("#ffffff"));
      dateLabel.setAlignment(Pos.CENTER);
      dateLabel.setPrefWidth(190);
      dateLabel.setPadding(new Insets(5, 0, 10, 0));


      card.getChildren().add(dateLabel);

      // Day section
      if (dayPeriod != null) {
        VBox daySection = new VBox(5);
        daySection.setAlignment(Pos.CENTER);
        daySection.setPadding(new Insets(5));

        Label dayTimeLabel = new Label("Day");
        dayTimeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        dayTimeLabel.setTextFill(Color.web("#ffffff"));

        // Day icon
        ImageView dayIcon = new ImageView();
        dayIcon.setFitHeight(40);
        dayIcon.setFitWidth(40);
        try {
          String iconPath = getWeatherIconPath(dayPeriod.shortForecast, false); // Day period
          InputStream is = getClass().getResourceAsStream(iconPath);
          if (is != null) {
            Image weatherImage = new Image(is);
            dayIcon.setImage(weatherImage);
          }
        } catch (Exception e) {
          // Fallback if image loading fails
        }

        // Day temperature - make sure it's visible
        Label dayTempLabel = new Label(dayPeriod.temperature + "°" + dayPeriod.temperatureUnit);
        dayTempLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        dayTempLabel.setTextFill(Color.WHITE);
        dayTempLabel.setAlignment(Pos.CENTER);
        dayTempLabel.setMaxWidth(Double.MAX_VALUE);

        // Day forecast - keep it concise and centered
        Label dayDescLabel = new Label(dayPeriod.shortForecast);
        dayDescLabel.setFont(Font.font("Verdana", 12));
        dayDescLabel.setWrapText(true);
        dayDescLabel.setMaxWidth(200);
        dayDescLabel.setTextFill(Color.WHITE);
        dayDescLabel.setAlignment(Pos.CENTER);
        dayDescLabel.setMaxWidth(Double.MAX_VALUE);


        // Day precipitation
        String dayPrecipText = "Precip: ";
        if (dayPeriod.probabilityOfPrecipitation != null && dayPeriod.probabilityOfPrecipitation.value > 0) {
          dayPrecipText += dayPeriod.probabilityOfPrecipitation.value + "%";
        } else {
          dayPrecipText += "0%";
        }
        Label dayPrecipLabel = new Label(dayPrecipText);
        dayPrecipLabel.setFont(Font.font("Verdana", 12));
        dayPrecipLabel.setTextFill(Color.WHITE);
        dayPrecipLabel.setAlignment(Pos.CENTER);
        dayPrecipLabel.setMaxWidth(Double.MAX_VALUE);

        // Day wind
        Label dayWindLabel = new Label("Wind: " + dayPeriod.windSpeed);
        dayWindLabel.setFont(Font.font("Verdana", 12));
        dayWindLabel.setTextFill(Color.WHITE);
        dayWindLabel.setWrapText(true);
        dayWindLabel.setAlignment(Pos.CENTER);
        dayWindLabel.setMaxWidth(Double.MAX_VALUE);

        daySection.getChildren().addAll(dayTimeLabel, dayIcon, dayTempLabel, dayDescLabel, dayPrecipLabel, dayWindLabel);

        // Add a background to make text more visible

        card.getChildren().add(daySection);
      }

      // Simple separator line
      if (dayPeriod != null && nightPeriod != null) {
        HBox separator = new HBox();
        separator.setPrefWidth(180);
        separator.setPrefHeight(20);separator.setAlignment(Pos.CENTER);
        separator.setPadding(new Insets(5, 0, 15, 0));

        // Create simple line
        Rectangle separatorLine = new Rectangle(180, 1);
        separatorLine.setFill(Color.WHITE);
        separator.setOpacity(0.5);

        // Add separator to container
        separator.getChildren().add(separatorLine);

        // Insert separator after the date label
        card.getChildren().add(1, separator);
      }

      // Night section
      if (nightPeriod != null) {
        VBox nightSection = new VBox(5);
        nightSection.setAlignment(Pos.CENTER);
        nightSection.setPadding(new Insets(5));

        // Label for night sections - "Tonight" for first card, "Night" for others
        Label nightTimeLabel = new Label(i == 0 ? "Tonight" : "Night");
        nightTimeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        nightTimeLabel.setTextFill(Color.web("#ffffff"));

        // Night icon
        ImageView nightIcon = new ImageView();
        nightIcon.setFitHeight(40);
        nightIcon.setFitWidth(40);
        try {
          String iconPath = getWeatherIconPath(nightPeriod.shortForecast, true); // Night period
          InputStream is = getClass().getResourceAsStream(iconPath);
          if (is != null) {
            Image weatherImage = new Image(is);
            nightIcon.setImage(weatherImage);
          }
        } catch (Exception e) {
          // Fallback if image loading fails
        }

        // Night temperature - make sure it's visible
        Label nightTempLabel = new Label(nightPeriod.temperature + "°" + nightPeriod.temperatureUnit);
        nightTempLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        nightTempLabel.setTextFill(Color.WHITE);
        nightTempLabel.setAlignment(Pos.CENTER);
        nightTempLabel.setMaxWidth(Double.MAX_VALUE);

        // Night forecast - keep it concise and centered and adjust "Sunny" to "Clear"
        String nightDescription = nightPeriod.shortForecast;
        // Replace "Sunny" with "Clear" for night periods
        if (nightDescription.toLowerCase().contains("sunny")) {
          nightDescription = nightDescription.toLowerCase().replace("sunny", "clear skies");
          // Capitalize first letter
          nightDescription = nightDescription.substring(0, 1).toUpperCase() +
              nightDescription.substring(1);        }
        Label nightDescLabel = new Label(nightDescription);
        nightDescLabel.setFont(Font.font("Verdana", 12));
        nightDescLabel.setWrapText(true);
        nightDescLabel.setMaxWidth(200);
        nightDescLabel.setTextFill(Color.WHITE);
        nightDescLabel.setAlignment(Pos.CENTER);
        nightDescLabel.setMaxWidth(Double.MAX_VALUE);
        nightDescLabel.setAlignment(Pos.CENTER);
        nightDescLabel.setMaxWidth(Double.MAX_VALUE);

        // Night precipitation
        String nightPrecipText = "Precip: ";
        if (nightPeriod.probabilityOfPrecipitation != null && nightPeriod.probabilityOfPrecipitation.value > 0) {
          nightPrecipText += nightPeriod.probabilityOfPrecipitation.value + "%";
        } else {
          nightPrecipText += "0%";
        }
        Label nightPrecipLabel = new Label(nightPrecipText);
        nightPrecipLabel.setFont(Font.font("Verdana", 12));
        nightPrecipLabel.setTextFill(Color.WHITE);
        nightPrecipLabel.setAlignment(Pos.CENTER);
        nightPrecipLabel.setMaxWidth(Double.MAX_VALUE);

        // Night wind
        Label nightWindLabel = new Label("Wind: " + nightPeriod.windSpeed);
        nightWindLabel.setFont(Font.font("Verdana", 12));
        nightWindLabel.setTextFill(Color.WHITE);
        nightWindLabel.setWrapText(true);
        nightWindLabel.setAlignment(Pos.CENTER);
        nightWindLabel.setMaxWidth(Double.MAX_VALUE);

        nightSection.getChildren().addAll(nightTimeLabel, nightIcon, nightTempLabel, nightDescLabel, nightPrecipLabel, nightWindLabel);

        // Add a horizontal separator line
        Rectangle separator = new Rectangle(180, 1);
        separator.setFill(Color.WHITE);
        separator.setOpacity(0.5);

        // Add the separator and night section
        card.getChildren().addAll(separator, nightSection);
      }

      forecastContainer.getChildren().add(card);
      day++;
    }

    scrollPane.setContent(forecastContainer);



    // Main layout
    BorderPane mainLayout = new BorderPane();
    mainLayout.setTop(header);

    VBox centerContent = new VBox(10);
    centerContent.setAlignment(Pos.CENTER);
    centerContent.getChildren().addAll(locationLabel, scrollPane);
    centerContent.setPadding(new Insets(10));
    mainLayout.setCenter(centerContent);
    mainLayout.setBottom(bottomNav);

    // Create scene with light gradient background
    forecastScene = new Scene(mainLayout, 1200, 700);
    mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #90caf9, #1a237e);");

  }
}
