import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class LangtonAnts extends PApplet {



int numberOfAnts; // Probabilidad de iniciar vivo 
int antConsentrationSquare; //50x50
int multicolor = 0;
int antBorn;
int numOfColors = 4;

int cellSize; // Tamaño de las celdas (PX)
float interval = 1; // TIMER (1)
int lastRecordedTime = 0; // TIMER (2)
int pause = 0;

int azul = color(51, 121, 180);
int verde = color(0, 200, 0);
int rosa = color(254, 0, 129);
int amarillo = color(243, 197, 13);
int morado = color(170, 0, 255);
int negro = color(0);
int piel = color(237, 160, 122);
int blanco = color(255,255,255);
int gris = color(200,200,200);

int colorAnt = rosa; // Color hormiga
int colorPaw = azul; // Color vivos
int colorPaw2 = amarillo; // Color vivos
int colorPaw3 = verde; // Color vivos

int colorDead = negro; // Color muertos/vacio

int[][] cells; // Matriz del juego
int[][] cellsBuffer; // Buffer del juego (Mientras se cambia la matriz principal, se usa esta) 

int dimX; //= width/cellSize;
int dimY; //= height/cellSize;

ArrayList<Ant> AntList = new ArrayList<Ant>();

int cellLive = 0;
int cellLiveAt1000 = 0;
int cellLiveSum = 0;
int cellLiveProm = 0;
int cellDead = 0;


int iterationCount = 0;

int itCount1000 = 0;
String Slive = "";
String Sdead = "";
String Sants = "";

PrintWriter output;
int outFiles = 0;

JSONObject json;

public void loadJSON(){

  json = loadJSONObject("config.json");
  
  numberOfAnts = json.getInt("numberOfAnts"); // Probabilidad de iniciar vivo 
  antConsentrationSquare = json.getInt("antConsentrationSquare"); //50x50
  antBorn = json.getInt("antBorn");
  cellSize= json.getInt("cellSize"); // Tamaño de las celdas (PX)
  dimX = json.getInt("dimX");
  dimY = json.getInt("dimY");
}

public void setup(){
  
  loadJSON();
  
  
  surface.setResizable(true);
  
  cells = new int[dimX][dimY]; //Inicia las matrices
  
  // Color de la grilla
  noStroke();
  
  
  int initX = (int)random(dimX) - antConsentrationSquare;
  int initY = (int)random(dimY) - antConsentrationSquare;
  if(initX < 0) initX = 0;
  if(initY < 0) initY = 0;
  //Agrega las hormigas
  for(int a=0; a < numberOfAnts; a++){
    int NewAntColor = colorAnt;
    int isqueen = 0;
    if(a == 0 && antBorn == 1){ 
      NewAntColor = verde; 
      isqueen = 1;
    }
    Ant newAnt = new Ant(NewAntColor, blanco, initX + (int)random(antConsentrationSquare), initY + (int)random(antConsentrationSquare), (int)random(4)+1, isqueen);
    AntList.add(newAnt);
  }
  
  //INICIA grilla
  cells = new int[dimX][dimY];
  
  
}

PFont myFont; 
String myText = "Lorem ipsum"; 

public void draw() {
  
  
  
cellDead = 0;
  //Dibuja la grilla
  for (int x=0; x<dimX; x++) {
    for (int y=0; y<dimY; y++) {
      
      if (cells[x][y]==1) {
        fill(colorPaw); // Vivo
      }else if (cells[x][y]==2) {
        fill(colorPaw2); // Vivo
      }else if (cells[x][y]==3) {
        fill(colorPaw3); // Vivo
      }else if (cells[x][y]==4) {
        fill(piel); // Vivo
      }else {
        fill(colorDead); // Muerto
        cellDead++;
      }
      for (Ant ant : AntList) {
        if(ant.x == x && ant.y == y){
          fill(ant.tcolor);
        }
        if(ant.x == x+1 && ant.y == y && ant.direction == 1){
          fill(ant.acolor);
        }
        if(ant.x == x-1 && ant.y == y && ant.direction == 3){
          fill(ant.acolor);
        }
        if(ant.x == x && ant.y == y+1 && ant.direction == 2){
          fill(ant.acolor);
        }
        if(ant.x == x && ant.y == y-1 && ant.direction == 4){
          fill(ant.acolor);
        }
      }
      //dibujar patrones encontrados
      rect (x*cellSize, y*cellSize, cellSize, cellSize);
    }
  }
  /*if (millis()-lastRecordedTime>interval) {
    if (pause == 0) {*/
      iteration();
      /*lastRecordedTime = millis();
    }
  }*/
  
  textSize(12);
  fill (blanco);
  text("Instituto Politécnico Nacional\nEscuela Superior de Cómputo\nComputing Selected Topics - Complex Systems",30,30);
  text("Profesor: Genaro Juárez Martínez\nAutor: Alejandro Hernández Gómez",750,30);
  
  text("Generación: "+iterationCount+"\nHormigas existentes: "+AntList.size()+"\nCelulas vivas: "+cellLive+"\t Tendencia: "+cellLiveProm+"\nCelulas muertas: "+cellDead+"\nCelulas vivas en la 1000 generación: "+cellLiveAt1000, 30, 900);
  text("Hormigas iniciales: "+numberOfAnts+"\nCompresión inicial: "+antConsentrationSquare+"x"+antConsentrationSquare+"\nAntborn: "+antBorn+"\nTamaño de las celdas: "+cellSize+"px",800, 900);
   
 
 
}

public void iteration() { // iteracion
  if(iterationCount == 1000){
    cellLiveAt1000 = cellLive;
  }
  if(itCount1000 == 1000){
    itCount1000 = 0;
    
    /*output = createWriter("data/Ants-"+outFiles+"-live.txt");
    output.println(Slive);
    output.flush(); // Writes the remaining data to the file
    output.close(); // Finishes the file
    
    output = createWriter("data/Ants-"+outFiles+"-dead.txt");
    output.println(Sdead);
    output.flush(); // Writes the remaining data to the file
    output.close(); // Finishes the file
    
    output = createWriter("data/Ants-"+outFiles+"-ants.txt");
    output.println(Sants);
    output.flush(); // Writes the remaining data to the file
    output.close(); // Finishes the file*/
    
    outFiles++;
    
    //Restart
    Slive = "";
    Sdead = "";
    Sants = "";
  }
  
  Sants += AntList.size()+",";
  Slive += cellLive+",";
  Sdead += cellDead+",";
  
  /*println("____________________________");
  println("Generación: "+iterationCount);
  println("Hormigas existentes: "+AntList.size());
  println("Celulas vivas: "+cellLive+"\t Tendencia: "+cellLiveProm);
  println("Celulas muertas: "+cellDead);
  if(iterationCount >= 1000){
    println("Celulas vivas en la 1000 generación: "+cellLiveAt1000);
  }*/
  
  int newAntis = 0;
  int newAntX = 0;
  int newAntY = 0;
  for (Ant ant : AntList) {
    ant.step();
    if(ant.isqueen==1){
      for (int i = 1; i < AntList.size(); i++) {
        Ant ant2 = AntList.get(i);
        if(ant2.isqueen==0){
          if(ant.x == ant2.x && ant.y == ant2.y){
            newAntX = ant.x;
            newAntY = ant.y;
            newAntis = 1;
          }
        }
      }
    }
  }
  if(newAntis == 1){
    newAntis = 0;
    Ant newAnt = new Ant(amarillo, blanco, newAntX, newAntY, (int)random(4)+1, 2);
    AntList.add(newAnt);
  }
  iterationCount++;
  itCount1000++;
  cellLiveSum += cellLive;
  cellLiveProm = cellLiveSum / iterationCount;
         
}

class Ant{
  int isqueen;
  int acolor;
  int tcolor;
  int x;
  int y;
  int direction; //1: left, 2: top; 3: right; 4: bottom;
  
  public Ant(int colora, int colort, int xpos,int ypos,int dir,int iqueen){
    acolor = colora;
    tcolor = colort;
    isqueen = iqueen;
    x = xpos;
    y = ypos;
    direction = dir;
  }
  
  public void turnLeft(){
    this.turn(-1);
  }
  
  public void turnRight(){
    this.turn(1);
  }
  
  public void turn(int dir){
    if(this.direction == 1){
      this.direction = (dir == 1)? 2:4;
    }else if(this.direction == 2){
      this.direction = (dir == 1)? 3:1;
    }else if(this.direction == 3){
      this.direction = (dir == 1)? 4:2;
    }else if(this.direction == 4){
      this.direction = (dir == 1)? 1:3;
    }
  }
  
  public void step(){
    if(cells[this.x][this.y] == 0){
      cells[this.x][this.y] = 1;
      cellLive++;
    }else if(cells[this.x][this.y] < 4 && multicolor == 1){
      cells[this.x][this.y]++;
      cellLive++;
    }else{
      cells[this.x][this.y] = 0;
      cellLive--;
    }

    if(this.direction == 1){ //LEFT
      if(this.x == 0){
        this.x = dimX - 1;
      }else{
        this.x = this.x - 1;
      }
    }else if(this.direction == 2){ //TOP
      if(this.y == 0){
        this.y = dimY - 1;
      }else{
        this.y = this.y - 1;
      }
    }else if(this.direction == 3){ //RIGHT
      if(this.x == dimX - 1){
        this.x = 0;
      }else{
        this.x = this.x + 1;
      }
    }else if(this.direction == 4){ //BOTTOM
      if(this.y == dimY - 1){
        this.y = 0;
      }else{
        this.y = this.y + 1;
      }
    }
    
    if(cells[this.x][this.y] == 0){
      this.turnRight();
    }else{
      this.turnLeft();
    }
  }
}
  public void settings() {  size (1000, 1000);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "LangtonAnts" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
