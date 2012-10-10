/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.text.ui;

/**
 * The columns layout computes the width of the columns in a table.
 */
public abstract class ColumnLayout {

  public static ColumnLayout rightToLeft() {
    return RTL;
  }

  public static ColumnLayout weighted(int... weights) throws NullPointerException, IllegalArgumentException {
    return new Weighted(weights);
  }

  abstract int[] compute(Border border, int width, int[] widths, int[] minWidths);

  public static class Weighted extends ColumnLayout {

    /** The weights. */
    private final int[] weights;

    /**
     * Create a new weighted layout.
     *
     * @param weights the weights
     * @throws NullPointerException if the weights argument is null
     * @throws IllegalArgumentException if any weight is negative
     */
    private Weighted(int... weights) throws NullPointerException, IllegalArgumentException {
      if (weights == null) {
        throw new NullPointerException("No null weights accepted");
      }
      for (int weight : weights) {
        if (weight < 0) {
          throw new IllegalArgumentException("No negative weight accepted");
        }
      }
      this.weights = weights.clone();
    }

    public int[] getWeights() {
      return weights.clone();
    }

    @Override
    int[] compute(Border border, int width, int[] widths, int[] minWidths) {

      //
      int len = Math.min(widths.length, weights.length);

      //
      for (int i = len;i > 0;i--) {

        //
        int totalWeight = 0;
        int width2 = width;
        for (int j = 0;j < i;j++) {
          totalWeight += weights[j];
          if (border != null) {
            if (j == 0) {
              width2 -= 2;
            } else {
              width2 -= 1;
            }
          }
        }

        //

        // Compute the width of each column
        int[] ret = new int[widths.length];
        for (int j = 0;j < i;j++) {
          int w = width2 * weights[j];
          if (w < minWidths[j] * totalWeight) {
            ret = null;
            break;
          } else {
            ret[j] = w;
          }
        }

        //
        if (ret != null) {
          // Error based scaling inspired from Brensenham algorithm:
          // => sum of the weights == width
          // => minimize error
          // for instance with "foo","bar" scaled to 11 chars
          // using floor + division gives "foobar_____"
          // this methods gives           "foo_bar____"
          int err = 0;
          for (int j = 0;j < ret.length;j++) {

            // Compute base value
            int value = ret[j] / totalWeight;

            // Lower value
            int lower = value * totalWeight;
            int errLower = err + ret[j] - lower;

            // Upper value
            int upper = lower + totalWeight;
            int errUpper = err + ret[j] - upper;

            // We choose between lower/upper according to the accumulated error
            // and we propagate the error
            if (Math.abs(errLower) < Math.abs(errUpper)) {
              ret[j] = value;
              err = errLower;
            } else {
              ret[j] = value + 1;
              err = errUpper;
            }
          }
          return ret;
        }
      }

      //
      return null;
    }
  }

  private static final ColumnLayout RTL = new ColumnLayout() {

    @Override
    int[] compute(Border border, int viewWidth, int[] colWidths, int[] colMinWidths) {

      //
      int[] ret = colWidths.clone();

      //
      int total = 0;
      for (int colWidth : colWidths) {
        if (border != null) {
          total += 1;
        }
        total += colWidth;
      }
      if (border != null) {
        total += 1;
      }

      //
      int index = colMinWidths.length - 1;
      while (total > viewWidth && index >= 0) {
        int delta = total - viewWidth;
        int bar = colWidths[index] - colMinWidths[index];
        if (delta <= bar) {
          int sub = Math.min(bar, delta);
          total -= sub;
          ret[index] -= sub;
        } else {
          int foo = colWidths[index];
          if (border != null) {
            foo++;
            if (index == 0) {
              foo++;
            }
          }
          total -= foo;
          ret[index] = 0;
        }
        index--;
      }

      //
      return total > 0 ? ret : null;
    }
  };
/*

  public static final ColumnLayout DISTRIBUTED = new ColumnLayout() {
    @Override
    public int[] compute(Border border, int width, int[] widths, int[] minWidths) {
      int index = 0;
      while (true) {

        // Compute now the number of chars
        boolean done = false;
        int total = 0;
        for (int i = 0;i < widths.length;i++) {
          if (widths[i] >= minWidths[i]) {
            total += widths[i];
            if (border != null) {
              if (done) {
                total++;
              }
              else {
                total += 2;
                done = true;
              }
            }
          }
        }

        // It's not valid
        if (total == 0) {
          return null;
        }

        //
        int delta = width - total;

        //
        if (delta == 0) {
          break;
        } else if (delta > 0) {
          switch (widths[index]) {
            case 0:
              break;
            default:
              widths[index]++;
              break;
          }
          index = (index + 1) % widths.length;
        } else {

          // First try to remove from a column above min size
          int found = -1;
          for (int i = widths.length - 1;i >= 0;i--) {
            int p = (index + i) % widths.length;
            if (widths[p] > minWidths[p]) {
              found = p;
              if (--index < 0) {
                index += widths.length;
              }
              break;
            }
          }

          // If we haven't found a victim then we consider removing a column
          if (found == -1) {
            for (int i = widths.length - 1;i >= 0;i--) {
              if (widths[i] > 0) {
                found = i;
                break;
              }
            }
          }

          // We couldn't find any solution we give up
          if (found == -1) {
            break;
          } else {
            widths[found]--;
          }
        }
      }

      //
      return widths;
    }
  };
*/
}
