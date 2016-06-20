/*
 * This file contains the custom projection declarations for leaflet based
 * thru proj4leaflet
 */



/**
 * EPSG:25830
 */
L.CRS.EPSG25830 = new L.Proj.CRS('EPSG:25830',
		'+proj=utm +zone=30 +ellps=GRS80 +units=m +no_defs',
		{
			resolutions: [
			              2048, 1024, 512, 256, 128,
			              64, 32, 16, 8, 4, 2, 1, 0.5, 0.2, 0.1, 0.05
			              ],
		origin: [0, 0]
		});
