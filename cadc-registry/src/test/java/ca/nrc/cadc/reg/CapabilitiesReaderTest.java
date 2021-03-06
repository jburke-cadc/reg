/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2019.                            (c) 2019.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
************************************************************************
 */

package ca.nrc.cadc.reg;

import ca.nrc.cadc.util.FileUtil;
import ca.nrc.cadc.util.Log4jInit;
import java.io.File;
import java.io.FileReader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author pdowler
 */
public class CapabilitiesReaderTest {

    private static final Logger log = Logger.getLogger(CapabilitiesReaderTest.class);

    static {
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.INFO);
    }

    public CapabilitiesReaderTest() {
    }

    @Test
    public void testRead() {
        try {
            File f = FileUtil.getFileFromResource("sample-capabilities.xml", CapabilitiesReaderTest.class);
            Assert.assertNotNull("test setup", f);

            CapabilitiesReader r = new CapabilitiesReader();
            Capabilities caps = r.read(new FileReader(f));
            Assert.assertNotNull(caps);
            Assert.assertEquals(4, caps.getCapabilities().size());

            Capability cap;

            cap = caps.findCapability(Standards.VOSI_AVAILABILITY);
            Assert.assertNotNull(cap);

            cap = caps.findCapability(Standards.VOSI_CAPABILITIES);
            Assert.assertNotNull(cap);

            cap = caps.findCapability(Standards.VOSI_TABLES_11);
            Assert.assertNotNull(cap);
            Interface ti = cap.findInterface(Standards.SECURITY_METHOD_ANON, Standards.INTERFACE_PARAM_HTTP);
            Assert.assertNotNull("anon tables", ti);
            Assert.assertEquals("http://example.net/srv/tables", ti.getAccessURL().getURL().toExternalForm());

            cap = caps.findCapability(Standards.TAP_10);
            Assert.assertNotNull(cap);

            Interface bi = cap.findInterface(Standards.SECURITY_METHOD_ANON, Standards.INTERFACE_PARAM_HTTP);
            Assert.assertNotNull("anon base", bi);
            Assert.assertEquals("http://example.net/srv", bi.getAccessURL().getURL().toExternalForm());

            bi = cap.findInterface(Standards.SECURITY_METHOD_CERT, Standards.INTERFACE_PARAM_HTTP);
            Assert.assertNotNull("cert base", bi);
            Assert.assertEquals("https://example.net/srv", bi.getAccessURL().getURL().toExternalForm());

            bi = cap.findInterface(Standards.SECURITY_METHOD_HTTP_BASIC, Standards.INTERFACE_PARAM_HTTP);
            Assert.assertNotNull("basic-aa base", bi);
            Assert.assertEquals("https://example.net/srv/up", bi.getAccessURL().getURL().toExternalForm());

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testMultipleSecurityMethods() {
        try {
            File f = FileUtil.getFileFromResource("multi-sec-capabilities.xml", CapabilitiesReaderTest.class);
            Assert.assertNotNull("test setup", f);

            CapabilitiesReader r = new CapabilitiesReader();
            Capabilities caps = r.read(new FileReader(f));
            Assert.assertNotNull(caps);
            Assert.assertEquals(1, caps.getCapabilities().size());

            Capability cap;

            cap = caps.findCapability(Standards.TAP_10);
            Assert.assertNotNull(cap);
            
            String expectedURL = "https://example.net/srv";

            Interface bi = cap.findInterface(Standards.SECURITY_METHOD_ANON, Standards.INTERFACE_PARAM_HTTP);
            Assert.assertNotNull("anon base", bi);
            Assert.assertEquals(expectedURL, bi.getAccessURL().getURL().toExternalForm());

            bi = cap.findInterface(Standards.SECURITY_METHOD_CERT, Standards.INTERFACE_PARAM_HTTP);
            Assert.assertNotNull("cert base", bi);
            Assert.assertEquals(expectedURL, bi.getAccessURL().getURL().toExternalForm());

            bi = cap.findInterface(Standards.SECURITY_METHOD_TOKEN, Standards.INTERFACE_PARAM_HTTP);
            Assert.assertNotNull("token base", bi);
            Assert.assertEquals(expectedURL, bi.getAccessURL().getURL().toExternalForm());

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
}
