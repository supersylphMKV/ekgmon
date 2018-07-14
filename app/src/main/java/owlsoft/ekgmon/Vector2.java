package owlsoft.ekgmon;

/**
 * Created by supersylph on 7/14/18.
 */

public class Vector2 {

    float _x;
    float _y;

    public Vector2(){
        this._x = 0;
        this._y = 0;
    }

    public Vector2(float x, float y){
        this._x = x;
        this._y = y;
    }

    public Vector2(Vector2 v){
        this._x = v.x();
        this._y = v.y();
    }

    public float x (){
        return this._x;
    }

    public float y (){
        return this._y;
    }

    public void Set(float x, float y){
        this._x = x;
        this._y = y;
    }

    public String ToString(){
        return "(" + this._x +"," + this._y + ")";
    }
}
