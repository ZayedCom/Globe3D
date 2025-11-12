package com.app.nfusion.globe3d;

import android.content.Context;

// Data class containing physical, orbital, and exploration information for celestial bodies
public class PlanetData {
    public String name; // Name of the celestial body
    public String mass; // Mass of the celestial body
    public String diameter; // Diameter from pole to pole
    public String radius; // Radius from center to surface
    public String gravity; // Surface gravity compared to Earth
    public String density; // Material density in kg/m³
    public String rotationPeriod; // Length of one day on the body
    public String orbitalPeriod; // Time to complete one orbit around the Sun
    public String axialTilt; // Tilt of rotation axis affecting seasons
    public String orbitalSpeed; // Average velocity around the Sun
    public String orbitalEccentricity; // How circular or elliptical the orbit is
    public String numberOfMoons; // Count of natural satellites
    public String rings; // Presence of rings around the body
    public String atmosphericComposition; // Main gases in the atmosphere
    public String averageSurfaceTemperature; // Average temperature in °C or Kelvin
    public String surfacePressure; // Atmospheric pressure at ground level
    public String colorAlbedo; // How much light the body reflects
    public String notableSurfaceFeatures; // Volcanoes, canyons, storms, ice caps, etc.
    public String discoveryDate; // When the body was discovered
    public String discoveredBy; // Name of astronomer or probe that discovered it
    public String missionsVisited; // Spacecraft that have visited or studied it

    // Get Earth data with all physical and orbital properties
    public static PlanetData getEarth(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.earth_name);
        data.mass = context.getString(R.string.earth_mass);
        data.diameter = context.getString(R.string.earth_diameter);
        data.radius = context.getString(R.string.earth_radius);
        data.gravity = context.getString(R.string.earth_gravity);
        data.density = context.getString(R.string.earth_density);
        data.rotationPeriod = context.getString(R.string.earth_rotation_period);
        data.orbitalPeriod = context.getString(R.string.earth_orbital_period);
        data.axialTilt = context.getString(R.string.earth_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.earth_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.earth_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.earth_number_of_moons);
        data.rings = context.getString(R.string.earth_rings);
        data.atmosphericComposition = context.getString(R.string.earth_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.earth_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.earth_surface_pressure);
        data.colorAlbedo = context.getString(R.string.earth_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.earth_notable_surface_features);
        data.discoveryDate = context.getString(R.string.earth_discovery_date);
        data.discoveredBy = context.getString(R.string.earth_discovered_by);
        data.missionsVisited = context.getString(R.string.earth_missions_visited);
        return data;
    }

    // Get Sun data with all physical and stellar properties
    public static PlanetData getSun(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.sun_name);
        data.mass = context.getString(R.string.sun_mass);
        data.diameter = context.getString(R.string.sun_diameter);
        data.radius = context.getString(R.string.sun_radius);
        data.gravity = context.getString(R.string.sun_gravity);
        data.density = context.getString(R.string.sun_density);
        data.rotationPeriod = context.getString(R.string.sun_rotation_period);
        data.orbitalPeriod = context.getString(R.string.sun_orbital_period);
        data.axialTilt = context.getString(R.string.sun_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.sun_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.sun_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.sun_number_of_moons);
        data.rings = context.getString(R.string.sun_rings);
        data.atmosphericComposition = context.getString(R.string.sun_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.sun_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.sun_surface_pressure);
        data.colorAlbedo = context.getString(R.string.sun_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.sun_notable_surface_features);
        data.discoveryDate = context.getString(R.string.sun_discovery_date);
        data.discoveredBy = context.getString(R.string.sun_discovered_by);
        data.missionsVisited = context.getString(R.string.sun_missions_visited);
        return data;
    }

    // Get Moon data with all physical and orbital properties
    public static PlanetData getMoon(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.moon_name);
        data.mass = context.getString(R.string.moon_mass);
        data.diameter = context.getString(R.string.moon_diameter);
        data.radius = context.getString(R.string.moon_radius);
        data.gravity = context.getString(R.string.moon_gravity);
        data.density = context.getString(R.string.moon_density);
        data.rotationPeriod = context.getString(R.string.moon_rotation_period);
        data.orbitalPeriod = context.getString(R.string.moon_orbital_period);
        data.axialTilt = context.getString(R.string.moon_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.moon_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.moon_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.moon_number_of_moons);
        data.rings = context.getString(R.string.moon_rings);
        data.atmosphericComposition = context.getString(R.string.moon_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.moon_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.moon_surface_pressure);
        data.colorAlbedo = context.getString(R.string.moon_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.moon_notable_surface_features);
        data.discoveryDate = context.getString(R.string.moon_discovery_date);
        data.discoveredBy = context.getString(R.string.moon_discovered_by);
        data.missionsVisited = context.getString(R.string.moon_missions_visited);
        return data;
    }
}

