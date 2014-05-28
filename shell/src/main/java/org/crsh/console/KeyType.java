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
package org.crsh.console;

import jline.console.Operation;

import java.util.HashMap;

/**
 * @author Julien Viet
 */
public enum KeyType {

  A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,
  a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,
  _0,_1,_2,_3,_4,_5,_6,_7,_8,_9,

  SPACE,

  UP,DOWN,LEFT,RIGHT,
  
  DELETE,BACKSPACE,ENTER,

  UNKNOWN

  ;

  private static KeyType[] INDEX = new KeyType[256];

  static {
    INDEX[' '] = SPACE;
    INDEX['0'] = _0;
    INDEX['1'] = _1;
    INDEX['2'] = _2;
    INDEX['3'] = _3;
    INDEX['4'] = _4;
    INDEX['5'] = _5;
    INDEX['6'] = _6;
    INDEX['7'] = _7;
    INDEX['8'] = _8;
    INDEX['9'] = _9;
    INDEX['A'] = A;
    INDEX['B'] = B;
    INDEX['C'] = C;
    INDEX['D'] = D;
    INDEX['E'] = E;
    INDEX['F'] = F;
    INDEX['G'] = G;
    INDEX['H'] = H;
    INDEX['I'] = I;
    INDEX['J'] = J;
    INDEX['K'] = K;
    INDEX['L'] = L;
    INDEX['M'] = M;
    INDEX['N'] = N;
    INDEX['O'] = O;
    INDEX['P'] = P;
    INDEX['Q'] = Q;
    INDEX['R'] = R;
    INDEX['S'] = S;
    INDEX['T'] = T;
    INDEX['U'] = U;
    INDEX['V'] = V;
    INDEX['W'] = W;
    INDEX['X'] = X;
    INDEX['Y'] = Y;
    INDEX['Z'] = Z;
    INDEX['a'] = a;
    INDEX['b'] = b;
    INDEX['c'] = c;
    INDEX['d'] = d;
    INDEX['e'] = e;
    INDEX['f'] = f;
    INDEX['g'] = g;
    INDEX['h'] = h;
    INDEX['i'] = i;
    INDEX['j'] = j;
    INDEX['k'] = k;
    INDEX['l'] = l;
    INDEX['m'] = m;
    INDEX['n'] = n;
    INDEX['o'] = o;
    INDEX['p'] = p;
    INDEX['q'] = q;
    INDEX['r'] = r;
    INDEX['s'] = s;
    INDEX['t'] = t;
    INDEX['u'] = u;
    INDEX['v'] = v;
    INDEX['w'] = w;
    INDEX['x'] = x;
    INDEX['y'] = y;
    INDEX['z'] = z;
  }

  /** . */

  public static KeyType forCodePoint(int codePoint) {
    if (codePoint >= 0 && codePoint < INDEX.length) {
      return INDEX[codePoint];
    } else {
      return null;
    }
  }

  static KeyType map(Operation operation, int[] sequence) {
    switch (operation) {
      case SELF_INSERT:
        if (sequence.length == 1) {
          int index = sequence[0];
          KeyType found = forCodePoint(index);
          if (found != null) {
            return found;
          }
        }
        break;
      case BACKWARD_CHAR:
        return LEFT;
      case FORWARD_CHAR:
        return RIGHT;
      case PREVIOUS_HISTORY:
        return UP;
      case NEXT_HISTORY:
        return DOWN;
      case BACKWARD_DELETE_CHAR:
        return BACKSPACE;
      case DELETE_CHAR:
        return DELETE;
      case ACCEPT_LINE:
        return ENTER;
    }
    return UNKNOWN;
  }

}
