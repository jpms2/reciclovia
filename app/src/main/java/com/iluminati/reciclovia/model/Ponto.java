package com.iluminati.reciclovia.model;

/**
 * Created by jpms2 on 28/05/2016.
 */
public class Ponto {

    private String name;
    private String coord;

    public Ponto(String name, String coord)
    {
        this.name = name;
        this.coord = coord;
    }

    public String getName()
    {
        return name;
    }

    public String getCoord()
    {
        return coord;
    }

}
