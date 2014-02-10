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

import java.util.Arrays;

/**
 * The layout computes the lengths of a list of contiguous cells.
 */
public abstract class Layout {

  public static Layout flow() {
    return RTL;
  }

  public static Layout weighted(int... weights) throws NullPointerException, IllegalArgumentException {
    return new Weighted(weights);
  }

  /**
   * Computes the list of lengths for the specifid list of cells with the following constraints:
   *
   * <ul>
   *   <li>the sum of the returned array elements must be equals to the <code>totalLength</code> argument</li>
   *   <li>a cell length should never be lesser than the provided minimum length</li>
   * </ul>
   *
   * The returned array is the list of lengths from left to right, the array size may be less than the
   * number of cells (i.e the size of the <code>actualLengths</code> and <code>minLengths</code> arguments). Missing
   * cells are just be discarded and not part of the resulting layout. Array should contain only positive values,
   * any zero length cell should be discarded. When cells must be discarded it must begin with the tail of the
   * list, i.e it is not allowed to discard a cell that does not have a successor.
   *
   *
   * @param spaced true if the cells are separated by one char
   * @param totalLength the total length of the line
   * @param actualLengths the actual length : an estimation of what cell's desired length
   * @param minLengths the minmum length : the length under which a cell cannot be rendered
   * @return the list of length.
   */
  abstract int[] compute(boolean spaced, int totalLength, int[] actualLengths, int[] minLengths);

  public static class Weighted extends Layout {

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
    int[] compute(boolean spaced, int length, int[] actualLengths, int[] minLengths) {

      //
      int count = Math.min(actualLengths.length, weights.length);

      //
      for (int i = count;i > 0;i--) {

        //
        int totalLength = length;
        int totalWeight = 0;
        for (int j = 0;j < i;j++) {
          totalWeight += weights[j];
          if (spaced) {
            if (j > 0) {
              totalLength--;
            }
          }
        }

        // Compute the length of each cell
        int[] ret = new int[i];
        for (int j = 0;j < i;j++) {
          int w = totalLength * weights[j];
          if (w < minLengths[j] * totalWeight) {
            ret = null;
            break;
          } else {
            ret[j] = w;
          }
        }

        //
        if (ret != null) {
          // Error based scaling inspired from Brensenham algorithm:
          // => sum of the weights == length
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

  private static final Layout RTL = new Layout() {

    @Override
    int[] compute(boolean spaced, int length, int[] actualLengths, int[] minLengths) {

      //
      int[] ret = actualLengths.clone();

      //
      int totalLength = 0;
      for (int i = 0;i < actualLengths.length;i++) {
        totalLength += actualLengths[i];
        if (spaced && i > 0) {
          totalLength++;
        }
      }

      //
      int index = minLengths.length - 1;
      while (totalLength > length && index >= 0) {
        int delta = totalLength - length;
        int bar = actualLengths[index] - minLengths[index];
        if (delta <= bar) {
          // We are done
          totalLength = length;
          ret[index] -= delta;
        } else {
          int foo = actualLengths[index];
          if (spaced) {
            if (index > 0) {
              foo++;
            }
          }
          totalLength -= foo;
          ret[index] = 0;
          index--;
        }
      }

      //
      if (totalLength > 0) {
        if (index == minLengths.length - 1) {
          return ret;
        } else {
          return Arrays.copyOf(ret, index + 1);
        }
      } else {
        return null;
      }
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
