# Plasmid Networking

Plasmid networking uses the custom payload packets of Minecraft. This document describes the networking with Plasmid.

## Specification

### Packet Specification

In this specification a packet is its own section, 
a description of what the packet is used for and how it should be handled,
then it specifies the direction (`C->S`, `C<-S`, `C<->S`),
and a table of the fields and type of the packet.

## Types

### BlockPos

`BlockPos` type represents a block position in the world,
`x` as a 26-bit integer, followed by `y` as a 12-bit integer, followed by `z` as a 26-bit integer (all signed, two's complement).
Currently, encoded as a 64-bit integer split into three parts:
 - x: 26 MSBs
 - z: 26 middle bits
 - y: 12 LSBs

### Bounds

Bounds represents a box which are defined with two [BlockPos]: a minimum and a maximum.

| Fields | Type       | Description             |
|:------:|:----------:|:------------------------|
| min    | [BlockPos] | The minimum coordinate. |
| max    | [BlockPos] | The maximum coordinate. |

### Region

A region represents a named box in a Plasmid map which can contain arbitrary data.

| Fields | Type       | Description                          |
|:------:|:----------:|:-------------------------------------|
| marker | string     | The name of the region.              |
| bounds | [Bounds]   | The bounds of the region.            |
| data   | NBT Tag    | Arbitrary data stored in the region. |

## Packets

### `plasmid:staging_map/enter`

Packet sent when a player enters a staging map editor. The client should render the bounds of the map if present,
and the contained regions, points, etc.  
A `plasmid:staging_map/regions` packet should be sent with this packet if the staging map contains regions.

Direction: `C<-S`

| Fields |  Type      | Description                                                                    |
|:------:|:----------:|:-------------------------------------------------------------------------------|
| id     | Identifier | The identifier of the entered staging map.                                     |
| bounds | [Bounds]   | The bounds of the staging map, may be `[[0, 0, 0], [0, 0, 0]]` if not present. |
| world  | Identifier | The world of the staging map.                                                  |
| data   | NBT Tag    | Arbitrary data assigned to the staging map.                                    |

### `plasmid:staging_map/region`

Specifies a single region to the client, the client should render a box, and a marker tag for it.

Direction: `C<-S`

| Fields | Type       | Description                                                            |
|:------:|:----------:|:-----------------------------------------------------------------------|
| map    | Identifier | The identifier of the staging map in which the region should be added. |
| id     | VarI32     | The region runtime identifier.                                         |
| region | [Region]   | The region to add.                                                     |

### `plasmid:staging_map/regions`

Specifies regions to the client, for each region the client should render a box, and a marker tag.

Direction: `C<-S`

| Fields  | Type                   | Description                                                         |
|:-------:|:----------------------:|:--------------------------------------------------------------------|
| map     | Identifier             | The identifier of the staging map containing the following regions. |
| count   | VarI32                 | The count of regions to update.                                     |
| regions | (VarI32, [Region])\[\] | The array of regions to update. The first of the pair is the runtime ID and the second the region data. |

### `plasmid:staging_map/region/add`

Requests the server to add a new region in the specified staging map.
The client should not assume the region exists until the server sends a `plasmid:staging_map/region` packet.

Direction: `C->S`

| Fields |  Type      | Description                                                            |
|:------:|:----------:|:-----------------------------------------------------------------------|
| map    | Identifier | The identifier of the staging map in which the region should be added. |
| region | [Region]   | The region to add.                                                     |

### `plasmid:staging_map/region/update`

Requests the server to update a region from the specified staging map.

Direction: `C->S`

| Fields |  Type      | Description                                                              |
|:------:|:----------:|:-------------------------------------------------------------------------|
| map    | Identifier | The identifier of the staging map in which the region should be updated. |
| id     | VarI32     | The region runtime ID.                                                   |
| region | [Region]   | The updated region.                                                      |

### `plasmid:staging_map/region/remove`

Remove a region from the specified staging map.
When sent from a client, this packet should be treated as a request which can be rejected.

Direction: `C<->S`

| Fields |  Type      | Description                                                              |
|:------:|:----------:|:-------------------------------------------------------------------------|
| map    | Identifier | The identifier of the staging map in which the region should be removed. |
| id     | VarI32     | The region runtime ID.                                                   |
| region | [Region]   | The region to remove.                                                    |

[BlockPos]: #blockpos
[Bounds]: #bounds
[Region]: #region
