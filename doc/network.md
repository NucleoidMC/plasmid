# Plasmid Networking

Plasmid networking uses the custom payload packets of Minecraft. This document describes the networking with Plasmid.

## Specification

### Packet Specification

In this specification a packet is its own section, 
a description of what the packet is used for and how it should be handled,
then it specifies the direction (`C->S`, `C<-S`, `C<->S`),
and a table of the fields and type of the packet.

## Types

Some data types used in this documentation are documented [here](https://wiki.vg/Protocol#Data_types).

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

### Array `A[]`

An array holds a list of values of the specified type.
It also contains the size `N` of the array.

| Fields | Type   | Description                                 |
|:------:|:------:|:--------------------------------------------|
| size   | VarI32 | The size of the array.                      |
| 0      | A      | The first element of the array if present.  |
| 1      | A      | The second element of the array if present. |
| ...    | A      | An element of the array if present.         |
| N-1    | A      | The N-1 element of the array if present.    |

### Tuple `(A, B, ...)`

A tuple is a type which holds the content of multiple types specified in its name.
The tuple type is very abstract and acts as many fields as the tuple should hold.

| Fields | Type | Description                    |
|:------:|:----:|:-------------------------------|
| first  | A    | The first value of the tuple.  |
| second | B    | The second value of the tuple. |
| ...    | ...  | Another value of the tuple.    |


## Packets

### `plasmid:workspace/enter`

Packet sent when a player enters a workspace editor. The client should render the bounds of the map if present,
and the contained regions, points, etc.  
A `plasmid:workspace/regions` packet should be sent with this packet if the workspace contains regions.

Direction: `C<-S`

| Fields |  Type      | Description                                                            |
|:------:|:----------:|:-----------------------------------------------------------------------|
| id     | Identifier | The identifier of the entered workspace.                               |
| bounds | [Bounds]   | The bounds of the map, may be `[[0, 0, 0], [0, 0, 0]]` if not present. |
| world  | Identifier | The world in which the workspace is present.                           |
| data   | NBT Tag    | Arbitrary data assigned to the map template.                           |

### `plasmid:workspace/region`

Specifies a single region, the client should render a box, and a marker tag for it.
If sent from a client, this is considered as an update request which can be rejected.

Direction: `C<->S`

| Fields    | Type       | Description                                                          |
|:---------:|:----------:|:---------------------------------------------------------------------|
| workspace | Identifier | The identifier of the workspace in which the region should be added. |
| id        | [VarI32]   | The region runtime identifier.                                       |
| region    | [Region]   | The region to add.                                                   |

### `plasmid:workspace/regions`

Specifies regions to the client, for each region the client should render a box, and a marker tag.

Direction: `C<-S`

| Fields    | Type                              | Description                                                       |
|:---------:|:---------------------------------:|:------------------------------------------------------------------|
| workspace | Identifier                        | The identifier of the workspace containing the following regions. |
| marker    | string                            | The marker of the regions to update.                              |
| regions   | ([VarI32], [Bounds], NBT Tag)\[\] | The array of regions to update. The tuple is, in order, `(runtime id, bounds, data)`. |

### `plasmid:workspace/region/add`

Requests the server to add a new region in the specified workspace.
The client should not assume the region exists until the server sends a `plasmid:workspace/region` packet.

Direction: `C->S`

| Fields    |  Type      | Description                                                          |
|:---------:|:----------:|:---------------------------------------------------------------------|
| workspace | Identifier | The identifier of the workspace in which the region should be added. |
| region    | [Region]   | The region to add.                                                   |

### `plasmid:workspace/region/remove`

Remove a region from the specified workspace.
When sent from a client, this packet should be treated as a request which can be rejected.

Direction: `C<->S`

| Fields    |  Type      | Description                                                            |
|:---------:|:----------:|:-----------------------------------------------------------------------|
| workspace | Identifier | The identifier of the workspace in which the region should be removed. |
| id        | [VarI32]   | The region runtime ID.                                                 |
| region    | [Region]   | The region to remove.                                                  |

[VarI32]: https://wiki.vg/Protocol#VarInt_and_VarLong "wiki.vg documentation"
[BlockPos]: https://wiki.vg/Protocol#Position "wiki.vg documentation"
[Bounds]: #bounds
[Region]: #region
