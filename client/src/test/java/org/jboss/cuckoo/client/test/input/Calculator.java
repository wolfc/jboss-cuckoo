/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.cuckoo.client.test.input;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Calculator
{
   public static void main(String args[]) throws IOException
   {
      System.out.print("Enter digit one: ");
      System.out.flush();
      DataInputStream in = new DataInputStream(System.in);
      String s = in.readLine();
      System.out.print("Enter digit two: ");
      System.out.flush();
      String s2 = in.readLine();

      System.out.println("Result: " + (Integer.parseInt(s) + Integer.parseInt(s2)));
   }
}
