// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.yoidles.android.bronze.source.proto;

import com.yoidles.android.bronze.R;
import com.yoidles.android.bronze.source.AbstractAstronomicalSource;
import com.yoidles.android.bronze.source.LineSource;
import com.yoidles.android.bronze.source.PointSource;
import com.yoidles.android.bronze.source.TextSource;
import com.yoidles.android.bronze.source.impl.LineSourceImpl;
import com.yoidles.android.bronze.source.impl.PointSourceImpl;
import com.yoidles.android.bronze.source.impl.TextSourceImpl;
import com.yoidles.android.bronze.source.proto.SourceProto.AstronomicalSourceProto;
import com.yoidles.android.bronze.source.proto.SourceProto.GeocentricCoordinatesProto;
import com.yoidles.android.bronze.source.proto.SourceProto.LabelElementProto;
import com.yoidles.android.bronze.source.proto.SourceProto.LineElementProto;
import com.yoidles.android.bronze.source.proto.SourceProto.PointElementProto;
import com.yoidles.android.bronze.units.GeocentricCoordinates;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the
 * {@link com.yoidles.android.bronze.source.AstronomicalSource} interface
 * from objects serialized as protocol buffers.
 *
 * @author Brent Bryan
 */
public class ProtobufAstronomicalSource extends AbstractAstronomicalSource {
  private static final Map<SourceProto.Shape, PointSource.Shape> shapeMap =
    new HashMap<SourceProto.Shape, PointSource.Shape>();

  static {
    shapeMap.put(SourceProto.Shape.CIRCLE, PointSource.Shape.CIRCLE);
    shapeMap.put(SourceProto.Shape.STAR, PointSource.Shape.CIRCLE);
    shapeMap.put(SourceProto.Shape.ELLIPTICAL_GALAXY, PointSource.Shape.ELLIPTICAL_GALAXY);
    shapeMap.put(SourceProto.Shape.SPIRAL_GALAXY, PointSource.Shape.SPIRAL_GALAXY);
    shapeMap.put(SourceProto.Shape.IRREGULAR_GALAXY, PointSource.Shape.IRREGULAR_GALAXY);
    shapeMap.put(SourceProto.Shape.LENTICULAR_GALAXY, PointSource.Shape.LENTICULAR_GALAXY);
    shapeMap.put(SourceProto.Shape.GLOBULAR_CLUSTER, PointSource.Shape.GLOBULAR_CLUSTER);
    shapeMap.put(SourceProto.Shape.OPEN_CLUSTER, PointSource.Shape.OPEN_CLUSTER);
    shapeMap.put(SourceProto.Shape.NEBULA, PointSource.Shape.NEBULA);
    shapeMap.put(SourceProto.Shape.HUBBLE_DEEP_FIELD, PointSource.Shape.HUBBLE_DEEP_FIELD);
  }

  private final AstronomicalSourceProto proto;
  private final Resources resources;

  // Lazily construct the names.
  private ArrayList<String> names;

  public ProtobufAstronomicalSource(AstronomicalSourceProto proto, Resources resources) {
    this.proto = proto;
    this.resources = resources;
  }

  @Override
  public synchronized ArrayList<String> getNames() {
    if (names == null) {
      names = new ArrayList<String>(proto.getNameIdsCount());
      for (int id : proto.getNameIdsList()) {
        names.add(resources.getString(id));
      }
    }
    return names;
  }

  @Override
  public GeocentricCoordinates getSearchLocation() {
    return getCoords(proto.getSearchLocation());
  }

  @Override
  public List<PointSource> getPoints() {
    if (proto.getPointCount() == 0) {
      return Collections.<PointSource>emptyList();
    }
    ArrayList<PointSource> points = new ArrayList<PointSource>(proto.getPointCount());
    for (PointElementProto element : proto.getPointList()) {
      points.add(new PointSourceImpl(getCoords(element.getLocation()),
          element.getColor(), element.getSize(), shapeMap.get(element.getShape())));
    }
    return points;
  }

  @Override
  public List<TextSource> getLabels() {
    if (proto.getLabelCount() == 0) {
      return Collections.<TextSource>emptyList();
    }
    ArrayList<TextSource> points = new ArrayList<TextSource>(proto.getLabelCount());
    for (LabelElementProto element : proto.getLabelList()) {
      points.add(new TextSourceImpl(getCoords(element.getLocation()),
          resources.getString(element.getStringIndex()),
          element.getColor(), element.getOffset(), element.getFontSize()));
    }
    return points;

  }

  @Override
  public List<LineSource> getLines() {
    if (proto.getLineCount() == 0) {
      return Collections.<LineSource>emptyList();
    }
    ArrayList<LineSource> points = new ArrayList<LineSource>(proto.getLineCount());
    for (LineElementProto element : proto.getLineList()) {
      ArrayList<GeocentricCoordinates> vertices =
          new ArrayList<GeocentricCoordinates>(element.getVertexCount());
      for (GeocentricCoordinatesProto elementVertex : element.getVertexList()) {
        vertices.add(getCoords(elementVertex));
      }
      points.add(new LineSourceImpl(element.getColor(), vertices, element.getLineWidth()));
    }
    return points;
  }

  private static GeocentricCoordinates getCoords(GeocentricCoordinatesProto proto) {
    return GeocentricCoordinates.getInstance(proto.getRightAscension(), proto.getDeclination());
  }
}
