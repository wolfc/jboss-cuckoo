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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class InheritableThreadLocalOutputStream extends FilterOutputStream
{
   private InheritableThreadLocal<OutputStream> threadLocal = new InheritableThreadLocal<OutputStream>();
   
   /**
    * Creates an output stream filter built on top of the specified
    * underlying output stream.
    *
    * @param out the underlying output stream to be assigned to
    *            the field <tt>this.out</tt> for later use, or
    *            <code>null</code> if this instance is to be
    *            created without an underlying stream.
    */
   public InheritableThreadLocalOutputStream(OutputStream out)
   {
      super(out);
   }

   public void setCurrent(OutputStream out)
   {
      if(out == null)
         threadLocal.remove();
      else
         threadLocal.set(out);
   }

   @Override
   public void write(int b) throws IOException
   {
      OutputStream current = threadLocal.get();
      if(current != null)
         current.write(b);
      else
         super.write(b);
   }

   @Override
   public void write(byte[] b) throws IOException
   {
      OutputStream current = threadLocal.get();
      if(current != null)
         current.write(b);
      else
         super.write(b);
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException
   {
      OutputStream current = threadLocal.get();
      if(current != null)
         current.write(b, off, len);
      else
         super.write(b, off, len);
   }
}
