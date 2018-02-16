package com.example.perseus14.vehicletracking;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dell pc on 25/12/2015.
 */
public class UserLocalStore {

    public static final String SP_NAME = "userDetails";

    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    public void storeUserData(ModelUser user) {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putString("name", user.name);
        userLocalDatabaseEditor.putString("password", user.password);
        userLocalDatabaseEditor.putString("email", user.email);
        userLocalDatabaseEditor.putInt("id", user.id);
        userLocalDatabaseEditor.putInt("bus_radius",user.bus_radius);
        userLocalDatabaseEditor.putString("mobile_num", user.mobile_num);

        userLocalDatabaseEditor.commit();
    }

    public void storeBusData(ModelBusData modelDriver) {

        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putString("bus_reg_num", modelDriver.bus_reg_num);
        userLocalDatabaseEditor.putString("password", modelDriver.password);
        userLocalDatabaseEditor.putString("bus_service_num", modelDriver.bus_service_num);
        userLocalDatabaseEditor.putFloat("bus_lat",(float) modelDriver.bus_lat);
        userLocalDatabaseEditor.putFloat("bus_long", (float) modelDriver.bus_long);
        userLocalDatabaseEditor.putInt("bus_occupancy_level",modelDriver.bus_occupancy_level);
        userLocalDatabaseEditor.putInt("id", modelDriver.id);

        userLocalDatabaseEditor.commit();
    }

    public void setBusLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putBoolean("loggedIn", loggedIn);
        userLocalDatabaseEditor.commit();
    }

    public void clearBusData() {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.clear();
        userLocalDatabaseEditor.commit();
    }
    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putBoolean("loggedIn", loggedIn);
        userLocalDatabaseEditor.commit();
    }

    public void clearUserData() {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.clear();
        userLocalDatabaseEditor.commit();
    }

    public ModelUser getLoggedInUser() {
        if (userLocalDatabase.getBoolean("loggedIn", false) == false) {
            return null;
        }
        ModelUser user = new ModelUser();

        user.name = userLocalDatabase.getString("name", "");
        user.password = userLocalDatabase.getString("password", "");
        user.email = userLocalDatabase.getString("email", "");
        user.id = userLocalDatabase.getInt("id", -1);
        user.bus_radius = userLocalDatabase.getInt("bus_radius", -1);
        user.mobile_num = userLocalDatabase.getString("mobile_num","");

        return user;
    }

    public ModelBusData getLoggedInBus() {
        if (userLocalDatabase.getBoolean("loggedIn", false) == false) {
            return null;
        }
        ModelBusData modelBusData = new ModelBusData();

        modelBusData.id = userLocalDatabase.getInt("id", -1);
        modelBusData.password = userLocalDatabase.getString("password", "");
        modelBusData.bus_service_num = userLocalDatabase.getString("bus_service_num", "");
        modelBusData.bus_occupancy_level = userLocalDatabase.getInt("bus_occupancy_level", -1);
        modelBusData.bus_lat = userLocalDatabase.getFloat("bus_lat", -1);
        modelBusData.bus_long = userLocalDatabase.getFloat("bus_long", -1);
        modelBusData.bus_reg_num = userLocalDatabase.getString("bus_reg_num", "");


        return modelBusData;
    }

}
