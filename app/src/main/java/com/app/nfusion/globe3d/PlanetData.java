package com.app.nfusion.globe3d;

import android.content.Context;

// Data class containing physical, orbital, and exploration information for celestial bodies
public class PlanetData {
    // Public instance variables - Physical properties
    public String name; // Name of the celestial body
    public String mass; // Mass of the celestial body
    public String diameter; // Diameter from pole to pole
    public String radius; // Radius from center to surface
    public String gravity; // Surface gravity compared to Earth
    public String density; // Material density in kg/m³
    public String rotationPeriod; // Length of one day on the body
    public String axialTilt; // Tilt of rotation axis affecting seasons

    // Public instance variables - Orbital properties
    public String orbitalPeriod; // Time to complete one orbit around the Sun
    public String orbitalSpeed; // Average velocity around the Sun
    public String orbitalEccentricity; // How circular or elliptical the orbit is
    public String numberOfMoons; // Count of natural satellites
    public String rings; // Presence of rings around the body

    // Public instance variables - Atmospheric and surface properties
    public String atmosphericComposition; // Main gases in the atmosphere
    public String averageSurfaceTemperature; // Average temperature in °C or Kelvin
    public String surfacePressure; // Atmospheric pressure at ground level
    public String colorAlbedo; // How much light the body reflects
    public String notableSurfaceFeatures; // Volcanoes, canyons, storms, ice caps, etc.

    // Public instance variables - Discovery and exploration
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

    // Get Mercury data with all physical and orbital properties
    public static PlanetData getMercury(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.mercury_name);
        data.mass = context.getString(R.string.mercury_mass);
        data.diameter = context.getString(R.string.mercury_diameter);
        data.radius = context.getString(R.string.mercury_radius);
        data.gravity = context.getString(R.string.mercury_gravity);
        data.density = context.getString(R.string.mercury_density);
        data.rotationPeriod = context.getString(R.string.mercury_rotation_period);
        data.orbitalPeriod = context.getString(R.string.mercury_orbital_period);
        data.axialTilt = context.getString(R.string.mercury_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.mercury_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.mercury_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.mercury_number_of_moons);
        data.rings = context.getString(R.string.mercury_rings);
        data.atmosphericComposition = context.getString(R.string.mercury_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.mercury_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.mercury_surface_pressure);
        data.colorAlbedo = context.getString(R.string.mercury_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.mercury_notable_surface_features);
        data.discoveryDate = context.getString(R.string.mercury_discovery_date);
        data.discoveredBy = context.getString(R.string.mercury_discovered_by);
        data.missionsVisited = context.getString(R.string.mercury_missions_visited);
        return data;
    }

    // Get Venus data with all physical and orbital properties
    public static PlanetData getVenus(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.venus_name);
        data.mass = context.getString(R.string.venus_mass);
        data.diameter = context.getString(R.string.venus_diameter);
        data.radius = context.getString(R.string.venus_radius);
        data.gravity = context.getString(R.string.venus_gravity);
        data.density = context.getString(R.string.venus_density);
        data.rotationPeriod = context.getString(R.string.venus_rotation_period);
        data.orbitalPeriod = context.getString(R.string.venus_orbital_period);
        data.axialTilt = context.getString(R.string.venus_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.venus_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.venus_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.venus_number_of_moons);
        data.rings = context.getString(R.string.venus_rings);
        data.atmosphericComposition = context.getString(R.string.venus_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.venus_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.venus_surface_pressure);
        data.colorAlbedo = context.getString(R.string.venus_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.venus_notable_surface_features);
        data.discoveryDate = context.getString(R.string.venus_discovery_date);
        data.discoveredBy = context.getString(R.string.venus_discovered_by);
        data.missionsVisited = context.getString(R.string.venus_missions_visited);
        return data;
    }

    // Get Mars data with all physical and orbital properties
    public static PlanetData getMars(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.mars_name);
        data.mass = context.getString(R.string.mars_mass);
        data.diameter = context.getString(R.string.mars_diameter);
        data.radius = context.getString(R.string.mars_radius);
        data.gravity = context.getString(R.string.mars_gravity);
        data.density = context.getString(R.string.mars_density);
        data.rotationPeriod = context.getString(R.string.mars_rotation_period);
        data.orbitalPeriod = context.getString(R.string.mars_orbital_period);
        data.axialTilt = context.getString(R.string.mars_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.mars_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.mars_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.mars_number_of_moons);
        data.rings = context.getString(R.string.mars_rings);
        data.atmosphericComposition = context.getString(R.string.mars_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.mars_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.mars_surface_pressure);
        data.colorAlbedo = context.getString(R.string.mars_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.mars_notable_surface_features);
        data.discoveryDate = context.getString(R.string.mars_discovery_date);
        data.discoveredBy = context.getString(R.string.mars_discovered_by);
        data.missionsVisited = context.getString(R.string.mars_missions_visited);
        return data;
    }

    // Get Jupiter data with all physical and orbital properties
    public static PlanetData getJupiter(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.jupiter_name);
        data.mass = context.getString(R.string.jupiter_mass);
        data.diameter = context.getString(R.string.jupiter_diameter);
        data.radius = context.getString(R.string.jupiter_radius);
        data.gravity = context.getString(R.string.jupiter_gravity);
        data.density = context.getString(R.string.jupiter_density);
        data.rotationPeriod = context.getString(R.string.jupiter_rotation_period);
        data.orbitalPeriod = context.getString(R.string.jupiter_orbital_period);
        data.axialTilt = context.getString(R.string.jupiter_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.jupiter_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.jupiter_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.jupiter_number_of_moons);
        data.rings = context.getString(R.string.jupiter_rings);
        data.atmosphericComposition = context.getString(R.string.jupiter_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.jupiter_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.jupiter_surface_pressure);
        data.colorAlbedo = context.getString(R.string.jupiter_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.jupiter_notable_surface_features);
        data.discoveryDate = context.getString(R.string.jupiter_discovery_date);
        data.discoveredBy = context.getString(R.string.jupiter_discovered_by);
        data.missionsVisited = context.getString(R.string.jupiter_missions_visited);
        return data;
    }

    // Get Saturn data with all physical and orbital properties
    public static PlanetData getSaturn(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.saturn_name);
        data.mass = context.getString(R.string.saturn_mass);
        data.diameter = context.getString(R.string.saturn_diameter);
        data.radius = context.getString(R.string.saturn_radius);
        data.gravity = context.getString(R.string.saturn_gravity);
        data.density = context.getString(R.string.saturn_density);
        data.rotationPeriod = context.getString(R.string.saturn_rotation_period);
        data.orbitalPeriod = context.getString(R.string.saturn_orbital_period);
        data.axialTilt = context.getString(R.string.saturn_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.saturn_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.saturn_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.saturn_number_of_moons);
        data.rings = context.getString(R.string.saturn_rings);
        data.atmosphericComposition = context.getString(R.string.saturn_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.saturn_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.saturn_surface_pressure);
        data.colorAlbedo = context.getString(R.string.saturn_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.saturn_notable_surface_features);
        data.discoveryDate = context.getString(R.string.saturn_discovery_date);
        data.discoveredBy = context.getString(R.string.saturn_discovered_by);
        data.missionsVisited = context.getString(R.string.saturn_missions_visited);
        return data;
    }

    // Get Uranus data with all physical and orbital properties
    public static PlanetData getUranus(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.uranus_name);
        data.mass = context.getString(R.string.uranus_mass);
        data.diameter = context.getString(R.string.uranus_diameter);
        data.radius = context.getString(R.string.uranus_radius);
        data.gravity = context.getString(R.string.uranus_gravity);
        data.density = context.getString(R.string.uranus_density);
        data.rotationPeriod = context.getString(R.string.uranus_rotation_period);
        data.orbitalPeriod = context.getString(R.string.uranus_orbital_period);
        data.axialTilt = context.getString(R.string.uranus_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.uranus_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.uranus_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.uranus_number_of_moons);
        data.rings = context.getString(R.string.uranus_rings);
        data.atmosphericComposition = context.getString(R.string.uranus_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.uranus_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.uranus_surface_pressure);
        data.colorAlbedo = context.getString(R.string.uranus_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.uranus_notable_surface_features);
        data.discoveryDate = context.getString(R.string.uranus_discovery_date);
        data.discoveredBy = context.getString(R.string.uranus_discovered_by);
        data.missionsVisited = context.getString(R.string.uranus_missions_visited);
        return data;
    }

    // Get Neptune data with all physical and orbital properties
    public static PlanetData getNeptune(Context context) {
        PlanetData data = new PlanetData();
        data.name = context.getString(R.string.neptune_name);
        data.mass = context.getString(R.string.neptune_mass);
        data.diameter = context.getString(R.string.neptune_diameter);
        data.radius = context.getString(R.string.neptune_radius);
        data.gravity = context.getString(R.string.neptune_gravity);
        data.density = context.getString(R.string.neptune_density);
        data.rotationPeriod = context.getString(R.string.neptune_rotation_period);
        data.orbitalPeriod = context.getString(R.string.neptune_orbital_period);
        data.axialTilt = context.getString(R.string.neptune_axial_tilt);
        data.orbitalSpeed = context.getString(R.string.neptune_orbital_speed);
        data.orbitalEccentricity = context.getString(R.string.neptune_orbital_eccentricity);
        data.numberOfMoons = context.getString(R.string.neptune_number_of_moons);
        data.rings = context.getString(R.string.neptune_rings);
        data.atmosphericComposition = context.getString(R.string.neptune_atmospheric_composition);
        data.averageSurfaceTemperature = context.getString(R.string.neptune_average_surface_temperature);
        data.surfacePressure = context.getString(R.string.neptune_surface_pressure);
        data.colorAlbedo = context.getString(R.string.neptune_color_albedo);
        data.notableSurfaceFeatures = context.getString(R.string.neptune_notable_surface_features);
        data.discoveryDate = context.getString(R.string.neptune_discovery_date);
        data.discoveredBy = context.getString(R.string.neptune_discovered_by);
        data.missionsVisited = context.getString(R.string.neptune_missions_visited);
        return data;
    }
}

