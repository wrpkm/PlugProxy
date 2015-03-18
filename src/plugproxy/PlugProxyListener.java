package plugproxy;

/**
 *   (c) Copyright 2001,2002 - Christopher A. Longo
 *   =======================================
 *
 *   PlugProxy is free software; you can redistribute this file and/or modify it
 *   under the terms of the GNU General Public License as published by the Free
 *   Software Foundation; either version 2 of the License, or (at your option)
 *   any later version.
 */

import plugproxy.util.HexDump;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Passes TCP/IP data from the localhost to remote system:port
 *
 * @author Christopher A. Longo (cal@cloud9.net)
 * @version 2.00 06.27.2002
 **/
public class PlugProxyListener implements Runnable {
	/** The parameter structure **/
	private Parameters params;
	/** The runner thread **/
	private Thread runner;
	/** Socket for listening **/
	private ServerSocket listener;
	/** If worker thread is enabled **/
	private boolean enabled = true;
	/** Used for pushing verbose information **/
	private ActionListener actionListener;

	private FileOutputStream logStream = null;

	/** Information event for verbose mode **/
	public static int EVENT_INFO = 0x01;
	/** Data event for sniff mode **/
	public static int EVENT_DATA = 0x02;
	/** Input stream flag **/
	public static int STREAM_IN = 0x04;
	/** Output stream flag **/
	public static int STREAM_OUT = 0x08;

	/** Used to manage the child connections **/
	private ThreadGroup group = new ThreadGroup("PlugProxyGroup");

	/**
	 * Constructor.
	 **/
	public PlugProxyListener(Parameters params) throws IOException {
		this.params = params;
		listener = new ServerSocket(params.localPort);

		if (this.params.logFilename.length() > 0) {
			this.logStream = new FileOutputStream(this.params.logFilename,this.params.logAppend);
		}

		runner = new Thread(this);
		runner.setPriority(Thread.NORM_PRIORITY - 1);
		runner.start();
	}

	/**
	 * The main threading routine.
	 **/
	public void run() {
		while (enabled) {
			try {
				Socket client = listener.accept();
				Socket server = new Socket(params.host, params.remotePort);

				new PlugProxyThread(client.toString(), server, client,
						STREAM_IN, group).start();
				new PlugProxyThread(server.toString(), client, server,
						STREAM_OUT, group).start();
			} catch (IOException e) {
				enabled = false;
			}
		}
	}

	/**
	 * Stops the listener from forwarding data to the remote host. Shuts down
	 * all child threads.
	 **/
	public void stop() {
		enabled = false;

		try {
			listener.close();
		} catch (IOException ignore) {
		}

		PlugProxyThread[] list = new PlugProxyThread[group.activeCount()];
		group.enumerate(list);

		for (int i = 0; i < list.length; i++) {
			PlugProxyThread thread = list[i];
			thread.disable();
		}
	}

	/**
	 * Fires off an event to all registered listeners.
	 *
	 * @param s
	 *            A String containing details of the event.
	 **/
	protected void fireEvent(String s, int modifier) {
		if (actionListener != null) {
			ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
					s, modifier);
			actionListener.actionPerformed(e);
		}
	}

	/**
	 * Adds an ActionListener to this object's listener list.
	 *
	 * @param l
	 *            The ActionListener to add.
	 **/
	public synchronized void addActionListener(ActionListener l) {
		if (l != null)
			actionListener = AWTEventMulticaster.add(actionListener, l);
	}

	/**
	 * Removes an ActionListener from this object's listener list.
	 *
	 * @param l
	 *            The ActionListener to remove.
	 **/
	public synchronized void removeActionListener(ActionListener l) {
		if (l != null)
			actionListener = AWTEventMulticaster.remove(actionListener, l);
	}

	/**
	 * Returns a String representation of this object.
	 *
	 * @return A a String representation of this object.
	 **/
	public String toString() {
		String s = null;

		try {
			s = InetAddress.getLocalHost() + ":" + params.localPort + "->"
					+ params.host + ":" + params.remotePort;
		} catch (UnknownHostException ignore) {
		}

		return s;
	}

	/**
	 * This inner class is the actual engine for fowarding the data to the
	 * remote host.
	 **/
	class PlugProxyThread extends Thread {
		/** The size of the buffer to pass data from in->out **/
		private static final int BUFFER_SIZE = 4096;
		/** The actual data buffer **/
		private byte[] buffer = new byte[BUFFER_SIZE];
		/** The input stream **/
		private InputStream in;
		/** The output stream **/
		private OutputStream out;
		/** Direction of the stream, STREAM_IN or STREAM_OUT **/
		private int direction;
		/** Source socket **/
		private Socket source;
		/** Dest socket **/
		private Socket dest;

		/**
		 * Constructor
		 *
		 * @param name
		 *            The name of the thread.
		 * @param source
		 *            The source stream (in).
		 * @param dest
		 *            The destination stream (out).
		 * @param direction
		 *            The direction of the stream, STREAM_IN or STREAM_OUT.
		 * @throws IOException
		 *             Thrown if the an IO exception occurs.
		 */
		public PlugProxyThread(String name, Socket source, Socket dest,
				int direction, ThreadGroup group) throws IOException {
			super(group, name);

			this.source = source;
			this.dest = dest;

			in = source.getInputStream();
			out = dest.getOutputStream();
			this.direction = direction;
			setPriority(NORM_PRIORITY - 1);
		}

		/**
		 * Overrides Thread.run() Forwards any data from the source to the dest.
		 * Simple as that.
		 */
		public void run() {
			int len = 0;

			fireEvent(getName() + ": Connection established.", EVENT_INFO
					| direction);

			try {
				int frequency = 0;
				while (enabled && (len = in.read(buffer)) != -1) // this is the
																	// heart of
																	// it all.
																	// Read a
																	// chunk of
																	// data...
				{
					if (this.direction == STREAM_IN && PlugProxyListener.this.params.outDelay) {
						frequency++;

						if (frequency >= params.outDelayFrequency) {
							frequency = 0;

							try {
								fireEvent("Sleeping for: " + params.outDelayTime
										+ "ms\n", EVENT_DATA | direction);


								if (PlugProxyListener.this.logStream != null) {
									synchronized (PlugProxyListener.this.logStream) {
										 try {
							                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
							                 PlugProxyListener.this.logStream.write(new String("\n[" + sdf.format(new Date()) + "]" ).getBytes());
											 PlugProxyListener.this.logStream.write(new String("\nOutgoing Sleep for: " + params.outDelayTime
														+ "ms\n").getBytes());
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
								}

								Thread.sleep(params.outDelayTime);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else if (this.direction == STREAM_OUT && PlugProxyListener.this.params.inDelay) {
							frequency++;

							if (frequency >= params.inDelayFrequency) {
								frequency = 0;

								try {
									fireEvent("Sleeping for: " + params.inDelayTime
											+ "ms\n", EVENT_DATA | direction);


									if (PlugProxyListener.this.logStream != null) {
										synchronized (PlugProxyListener.this.logStream) {
											 try {
								                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
								                 PlugProxyListener.this.logStream.write(new String("\n[" + sdf.format(new Date()) + "]" ).getBytes());
												 PlugProxyListener.this.logStream.write(new String("\nIncoming Sleep for: " + params.inDelayTime
															+ "ms\n").getBytes());
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
										}
									}

									Thread.sleep(params.inDelayTime);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

					if (PlugProxyListener.this.logStream != null) {
						synchronized (PlugProxyListener.this.logStream) {
							 try {
				                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				                 PlugProxyListener.this.logStream.write(new String("[" + sdf.format(new Date()) + "]" + '\n').getBytes());
								 PlugProxyListener.this.logStream.write(buffer,0,len);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}

					out.write(buffer, 0, len); // ... and forward it out.
												// Everything else is just
												// logging and cleanup

					if (params.sniff) {
						fireEvent(
								(params.hex) ? HexDump.toHex(buffer, len) + '\n' // hexdump
																					// mode
										: new String(buffer, 0, len),
								EVENT_DATA | direction); // text mode
					}
				}
			} catch (IOException ignore) {
			} finally {
				cleanUp();
				fireEvent(getName() + ": Connection terminated.", EVENT_INFO
						| direction);
			}
		}

		/**
		 * Stops this thread from further prosessing data. Closes the blocking
		 * input stream.
		 */
		public void disable() {
			cleanUp();
		}

		/**
		 * Cleans up and closes all connections.
		 */
		private void cleanUp() {
			try {
				out.flush();
			} catch (Exception ignore) {
			}
			try {
				out.close();
			} catch (Exception ignore) {
			}
			try {
				in.close();
			} catch (Exception ignore) {
			}
			try {
				dest.close();
			} catch (Exception ignore) {
			}
			try {
				source.close();
			} catch (Exception ignore) {
			}

			in = null;
			out = null;
			source = null;
			dest = null;
		}
	}
}
