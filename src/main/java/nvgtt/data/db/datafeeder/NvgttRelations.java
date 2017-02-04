package nvgtt.data.db.datafeeder;

import org.neo4j.graphdb.RelationshipType;

public enum NvgttRelations implements RelationshipType {
	LinksTo, RedirectsTo, ConnectsTo
}
