/*
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2007 - 2015 Fiji
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package LocalThickness;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.StackStatistics;

/**
 * A class which can be used to programmatically run and control the various
 * steps in local thickness map calculations. +
 * 
 * @author Richard Domander
 */
public class LocalThicknessWrapper implements PlugIn {

	private static final String DEFAULT_TITLE_SUFFIX = "_LocThk";
	private static final String DEFAULT_TITLE = "ThicknessMap";
	private static final EDT_S1D geometryToDistancePlugin = new EDT_S1D();
	private static final Distance_Ridge distanceRidgePlugin =
		new Distance_Ridge();
	private static final Local_Thickness_Parallel localThicknessPlugin =
		new Local_Thickness_Parallel();
	private static final Clean_Up_Local_Thickness thicknessCleaningPlugin =
		new Clean_Up_Local_Thickness();
	private static final MaskThicknessMapWithOriginal thicknessMask =
		new MaskThicknessMapWithOriginal();

	/**
	 * A pixel is considered to be a part of the background if its color <
	 * threshold
	 */
	public int threshold = EDT_S1D.DEFAULT_THRESHOLD;

	/**
	 * Inverts thresholding so that pixels with values >= threshold are considered
	 * background.
	 */
	public boolean inverse = false;//EDT_S1D.DEFAULT_INVERSE;

	/**
	 * Controls whether the Thickness map gets masked with the original @see
	 * MaskThicknessMapWithOriginal
	 */
	public boolean maskThicknessMap = true;

	/**
	 * Controls whether the pixel values in the Thickness map get scaled @see
	 * LocalThicknessWrapper.calibratePixels()
	 */
	public boolean calibratePixels = false;

	private ImagePlus resultImage = null;
	private boolean showOptions = false;
	private String titleSuffix = DEFAULT_TITLE_SUFFIX;

	public LocalThicknessWrapper() {
		setSilence(true);
		geometryToDistancePlugin.showOptions = showOptions;
	}

	/**
	 * Controls whether the options dialog in EDT_S1D is shown to the user before
	 * processing starts.
	 *
	 * @param show If true, then the dialog is shown.
	 */
	public void setShowOptions(final boolean show) {
		showOptions = show;
		geometryToDistancePlugin.showOptions = show;
	}

	/**
	 * Sets whether intermediate images are shown to the user, or only the final
	 * result
	 *
	 * @param silent If true, then no intermediate images are shown in the GUI.
	 */
	public void setSilence(final boolean silent) {
		distanceRidgePlugin.runSilent = silent;
		localThicknessPlugin.runSilent = silent;
		thicknessCleaningPlugin.runSilent = silent;
		geometryToDistancePlugin.runSilent = silent;
	}

	/**
	 * Creates a thickness map from the given image
	 *
	 * @param inputImage An 8-bit binary image
	 * @return A 32-bit floating point thickness map image
	 */
	public ImagePlus processImage(final ImagePlus inputImage) {
		String originalTitle = Local_Thickness_Driver.stripExtension(inputImage
			.getTitle());
		final ImagePlus image = inputImage.duplicate();

		resultImage = null;

		if (originalTitle == null || originalTitle.isEmpty()) {
			originalTitle = DEFAULT_TITLE;
		}

		geometryToDistancePlugin.setup("", image);
		if (!showOptions) {
			// set options programmatically
			geometryToDistancePlugin.inverse = inverse;
			geometryToDistancePlugin.thresh = threshold;

			geometryToDistancePlugin.run(null);
		}
		else {
			// get options from the dialog in EDT_S1D
			geometryToDistancePlugin.run(null);

			if (geometryToDistancePlugin.gotCancelled()) {
				resultImage = null;
				return getResultImage();
			}

			inverse = geometryToDistancePlugin.inverse;
			threshold = geometryToDistancePlugin.thresh;
		}
		resultImage = geometryToDistancePlugin.getResultImage();

		distanceRidgePlugin.setup("", resultImage);
		distanceRidgePlugin.run(null);
		resultImage = distanceRidgePlugin.getResultImage();

		localThicknessPlugin.setup("", resultImage);
		localThicknessPlugin.run(null);
		resultImage = localThicknessPlugin.getResultImage();
		
		thicknessCleaningPlugin.setup("", resultImage);
		thicknessCleaningPlugin.run(null);
		resultImage = thicknessCleaningPlugin.getResultImage();

		if (maskThicknessMap) {
			thicknessMask.inverse = inverse;
			thicknessMask.threshold = threshold;
			resultImage = thicknessMask.trimOverhang(image, resultImage);
		}

		resultImage.setTitle(originalTitle + titleSuffix);
		resultImage.copyScale(image);
		if (calibratePixels) {
			calibratePixels();
		}

		return getResultImage();
	}

	private void calibratePixels() {
		pixelValuesToCalibratedValues();
		backgroundToNaN(0x00);
	}

	/**
	 * Sets the value of the background pixels in the result image to Float.NaN.
	 * Doing this prevents them from affecting statistical measures calculated
	 * from the image, e.g. mean pixel value.
	 *
	 * @param backgroundColor The color used to identify background pixels
	 *          (usually 0x00)
	 * @throws NullPointerException If this.resultImage == null
	 */
	private void backgroundToNaN(final int backgroundColor) {
		if (resultImage == null) {
			throw new NullPointerException(
				"The resultImage in LocalThicknessWrapper is null");
		}

		final int depth = resultImage.getNSlices();
		final int pixelsPerSlice = resultImage.getWidth() * resultImage.getHeight();
		final ImageStack stack = resultImage.getStack();

		for (int z = 1; z <= depth; z++) {
			final float pixels[] = (float[]) stack.getPixels(z);
			for (int i = 0; i < pixelsPerSlice; i++) {
				if (Float.compare(pixels[i], backgroundColor) == 0) {
					pixels[i] = Float.NaN;
				}
			}
		}
	}

	/**
	 * Multiplies all pixel values in the result image by pixelWidth. This ways
	 * the pixel values represent sample thickness in real units.
	 *
	 * @throws NullPointerException If this.resultImage == null
	 */
	private void pixelValuesToCalibratedValues() {
		if (resultImage == null) {
			throw new NullPointerException(
				"The resultImage in LocalThicknessWrapper is null");
		}

		final double pixelWidth = resultImage.getCalibration().pixelWidth;
		final ImageStack stack = resultImage.getStack();
		final int depth = stack.getSize();

		for (int z = 1; z <= depth; z++) {
			stack.getProcessor(z).multiply(pixelWidth);
		}

		final StackStatistics stackStatistics = new StackStatistics(resultImage);
		final double maxPixelValue = stackStatistics.max;
		resultImage.getProcessor().setMinAndMax(0, maxPixelValue);
	}

	/**
	 * @param imageTitleSuffix The suffix that's added to the end of the title of
	 *          the resulting thickness map image
	 */
	public void setTitleSuffix(final String imageTitleSuffix) {
		titleSuffix = imageTitleSuffix;

		if (titleSuffix == null || titleSuffix.isEmpty()) {
			titleSuffix = DEFAULT_TITLE_SUFFIX;
		}

		assert titleSuffix != null && !this.titleSuffix.isEmpty();
	}

	/**
	 * @return The thickness map from the last run of the plugin. Null if
	 *         something went wrong, e.g. the user cancelled the plugin.
	 */
	public ImagePlus getResultImage() {
		return resultImage;
	}

	@Override
	public void run(final String s) {
		ImagePlus image;

		try {
			image = IJ.getImage();
		}
		catch (final RuntimeException rte) {
			return; // no image currently open
		}

		processImage(image);

		if (resultImage == null) {
			return;
		}

		WindowManager.setTempCurrentImage(resultImage);
		//resultImage.show();
		//IJ.run("Fire"); // changes the color palette of the output image
	}
}