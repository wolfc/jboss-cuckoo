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
package org.jboss.cuckoo.server.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class InheritableThreadLocalInputStream extends FilterInputStream
{
   private InheritableThreadLocal<InputStream> threadLocal = new InheritableThreadLocal<InputStream>();

   /**
    * Creates a <code>FilterInputStream</code>
    * by assigning the  argument <code>in</code>
    * to the field <code>this.in</code> so as
    * to remember it for later use.
    *
    * @param in the underlying input stream, or <code>null</code> if
    *           this instance is to be created without an underlying stream.
    */
   public InheritableThreadLocalInputStream(InputStream in)
   {
      super(in);
   }

   @Override
   public int read() throws IOException
   {
      InputStream current = threadLocal.get();
      if(current != null)
         return current.read();
      return super.read();
   }

   @Override
   public int read(byte[] b) throws IOException
   {
      InputStream current = threadLocal.get();
      if(current != null)
         return current.read(b);
      return super.read(b);
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException
   {
      InputStream current = threadLocal.get();
      if(current != null)
         return current.read(b, off, len);
      return super.read(b, off, len);
   }

   public void setCurrent(InputStream in)
   {
      if(in == null)
         threadLocal.remove();
      else
         threadLocal.set(in);
   }
}
