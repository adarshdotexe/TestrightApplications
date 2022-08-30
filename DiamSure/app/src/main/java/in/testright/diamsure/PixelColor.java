package in.testright.diamsure;

public class PixelColor {
    int blue;
    int green;
    int red;
    int x;
    int y;

    public PixelColor(int red2, int blue2, int green2) {
        this.red = red2;
        this.blue = blue2;
        this.green = green2;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x2) {
        this.x = x2;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y2) {
        this.y = y2;
    }

    public int getRed() {
        return this.red;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getGreen() {
        return this.green;
    }
}
