/*
 * JDiff - HTML report of API differences
 * Copyright (C) 2021  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package jdiff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads in lines from an input stream and displays them.
 *
 * @author Matthew Doar, mdoar@pobox.com.
 */
class StreamReader extends Thread {
    /**
     * The input stream.
     */
    InputStream is_;

    /**
     * Constructor which takes an InputStream.
     */
    StreamReader(InputStream is) {
        is_ = is;
    }

    /**
     * Method which is called when this thread is started.
     */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is_);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null)
                System.out.println(line);
        } catch (IOException ioe) {
            System.out.println("IO Error invoking Javadoc");
            ioe.printStackTrace();
        } catch (Exception e) {
            // Ignore read errors which indicate that the process is complete
        }
    }
}
