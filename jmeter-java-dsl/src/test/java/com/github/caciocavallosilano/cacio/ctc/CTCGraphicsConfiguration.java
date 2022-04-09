package com.github.caciocavallosilano.cacio.ctc;

import com.github.caciocavallosilano.cacio.peer.managed.FullScreenWindowFactory;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;

/*
 * This code is copied from original cacio code but initializing transforms to avoid NPEs with
 * Darklaf.
 */
public class CTCGraphicsConfiguration extends GraphicsConfiguration {

  private CTCGraphicsDevice device;

  CTCGraphicsConfiguration(CTCGraphicsDevice dev) {
    device = dev;
  }

  @Override
  public GraphicsDevice getDevice() {
    return device;
  }

  @Override
  public ColorModel getColorModel() {
    return ColorModel.getRGBdefault();
  }

  @Override
  public ColorModel getColorModel(int transparency) {
    return ColorModel.getRGBdefault();
  }

  @Override
  public AffineTransform getDefaultTransform() {
    return new AffineTransform();
  }

  @Override
  public AffineTransform getNormalizingTransform() {
    return new AffineTransform();
  }

  @Override
  public Rectangle getBounds() {
    Dimension d = FullScreenWindowFactory.getScreenDimension();
    return new Rectangle(0, 0, d.width, d.height);
  }

}