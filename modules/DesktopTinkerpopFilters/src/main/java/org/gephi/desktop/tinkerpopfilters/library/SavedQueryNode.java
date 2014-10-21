/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Sébastien Heymann <sebastien.heymann@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.desktop.tinkerpopfilters.library;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.tinkerpopfilters.api.FilterController;
import org.gephi.tinkerpopfilters.api.FilterLibrary;
import org.gephi.tinkerpopfilters.api.Query;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class SavedQueryNode extends AbstractNode {

    private Query query;

    public SavedQueryNode(Query query) {
        super(Children.LEAF);
        this.query = query;
        setDisplayName(getQueryName(query));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{new RemoveAction()};
    }

    @Override
    public Action getPreferredAction() {
        return SavedQueryNodeDefaultAction.instance;
    }

    public Query getQuery() {
        return query;
    }

    private String getQueryName(Query query) {
        String res = query.getName();
        if (query.getPropertiesCount() > 0) {
            res += "(";
            for (int i = 0; i < query.getPropertiesCount(); i++) {
                res += "'" + query.getPropertyValue(i).toString() + "'";
                res += (i + 1 < query.getPropertiesCount()) ? "," : "";
            }
        }
        if (query.getChildren() != null) {
            if (query.getPropertiesCount() == 0) {
                res += "(";
            } else {
                res += ",";
            }
            for (Query child : query.getChildren()) {
                res += getQueryName(child);
                res += ",";
            }
            if (res.endsWith(",")) {
                res = res.substring(0, res.length() - 1);
            }
        }
        res += ")";
        return res;
    }

    private class RemoveAction extends AbstractAction {

        public RemoveAction() {
            super(NbBundle.getMessage(SavedQueryNode.class, "SavedQueryNode.actions.remove"));
        }

        public void actionPerformed(ActionEvent e) {
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            FilterLibrary filterLibrary = filterController.getModel().getLibrary();
            filterLibrary.deleteQuery(query);
        }
    }
}
