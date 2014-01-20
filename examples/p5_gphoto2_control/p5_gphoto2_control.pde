import net.sabamiso.processing.p5_gphoto2_control.*;

GPhoto2Control gphoto2;  
PImage capture_image;

public void setup() {
  size(640, 480);

  gphoto2 = new GPhoto2Control(this);
  boolean rv = gphoto2.connect();
  if (rv == false) {
    println("error: GPhoto2Control.connect() failed...");
    return;
  }
}

public void draw() {
  if (capture_image != null) {
    image(capture_image, 0, 0, width, height);
  }
}

public void mousePressed() {
  take_picture();
}

public void keyPressed() {
  switch(key) {
  case ' ':
    take_picture();
    break;
  }
}

public void take_picture() {
  capture_image = gphoto2.takePicture();
}

