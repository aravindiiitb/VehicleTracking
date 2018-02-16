package com.example.perseus14.vehicletracking;

import java.io.Serializable;

/**
 * Created by dell pc on 17/04/2016.
 */
public class ModelBusData implements Serializable {
    public int id;
    public String bus_reg_num;
    public String password;
    public String bus_service_num;
    public double bus_lat;
    public double bus_long;
    public int bus_occupancy_level;

}
