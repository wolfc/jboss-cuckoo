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
package org.jboss.cuckoo.server;

import org.jboss.cuckoo.common.io.Channels;
import org.jboss.cuckoo.common.io.MuxOutputStream;
import org.jboss.cuckoo.server.io.InheritableThreadLocalInputStream;
import org.jboss.cuckoo.server.io.InheritableThreadLocalOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Server
{
   private InheritableThreadLocalInputStream in;
   private InheritableThreadLocalOutputStream out;
   private InheritableThreadLocalOutputStream err;

   protected void accept(final Socket socket) throws IOException
   {
      System.out.println("Servicing " + socket);
      final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
      final OutputStream myOut = new MuxOutputStream(Channels.OUT, out);
      final OutputStream myErr = new MuxOutputStream(Channels.ERR, out);
      Callable<Void> callable = new Callable<Void>()
      {
         @Override
         public Void call() throws Exception
         {
            Server.this.in.setCurrent(socket.getInputStream());
            Server.this.out.setCurrent(myOut);
            Server.this.err.setCurrent(myErr);
            try
            {
               String s;
               if((s = reader.readLine()) != null)
               {
                  StringTokenizer st = new StringTokenizer(s);
                  List<String> args = new ArrayList<String>();
                  while(st.hasMoreTokens())
                     args.add(st.nextToken());
                  process(args.toArray(new String[0]));
               }
               return null;
            }
            catch(Exception e)
            {
               e.printStackTrace();
               throw e;
            }
            finally
            {
               socket.close();
            }
         }
      };
      FutureTask<Void> task = new FutureTask<Void>(callable);
      Thread thread = new Thread(task);
      thread.start();
   }

   /**
    * Run the Cuckoo server standalone.
    * 
    * @param args
    */
   public static void main(String args[]) throws Exception
   {
      Server server = new Server();
      server.start();
   }

   private void process(String... args) throws Exception
   {
      if(args == null || args.length == 0)
         return;

      if(args[0].equals("E"))
      {
         URL url = new URL(args[1]);
         String className = args[2];

         URLClassLoader cl = new URLClassLoader(urls(url));
         Class<?> cls = cl.loadClass(className);

         // TODO: arguments
         String mainArgs[] = { };
         
         Class<?> parameterTypes[] = { String[].class };
         Method method = cls.getMethod("main", parameterTypes);
         method.invoke(null, (Object) mainArgs);
      }
      else
         throw new IllegalArgumentException(Arrays.asList(args).toString());
   }

   public void start() throws Exception
   {
      this.in = new InheritableThreadLocalInputStream(System.in);
      System.setIn(in);
      this.out = new InheritableThreadLocalOutputStream(System.out);
      System.setOut(new PrintStream(out));
      this.err = new InheritableThreadLocalOutputStream(System.err);
      System.setErr(new PrintStream(err));

      int port = 45451;
      int backlog = 5;
//      InetAddress bindAddr = InetAddress.getByName("localhost");
      InetAddress bindAddr = null;
      final ServerSocket socket = new ServerSocket(port, backlog, bindAddr);

      Callable<Void> callable = new Callable<Void>()
      {
         @Override
         public Void call() throws Exception
         {
            while(true)
            {
               try
               {
                  accept(socket.accept());
               }
               catch(Exception e)
               {
                  e.printStackTrace();
               }
            }
         }
      };
      FutureTask<Void> task = new FutureTask<Void>(callable);
      Thread thread = new Thread(task);
      thread.start();
   }

   private static URL[] urls(URL... urls)
   {
      return urls;
   }
}
