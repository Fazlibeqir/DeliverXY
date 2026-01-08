import { Http } from "@nativescript/core";

export async function searchAddress(query: string) {
  if (!query || query.length < 3) return [];

  const url =
    "https://nominatim.openstreetmap.org/search" +
    "?format=json" +
    "&limit=5" +
    "&addressdetails=1" +
    "&countrycodes=mk" +
    "&q=" + encodeURIComponent(query + ", Skopje");

  try {
    const res = await Http.request({
      url,
      method: "GET",
      headers: {
        "User-Agent": "DeliverXY/1.0 (contact@deliverxy.app)",
        "Accept": "application/json"
      }
    });

    const raw = res.content?.toJSON();

    const data: any[] = Array.isArray(raw) ? raw : [];

    return data
      .filter(r => r.type !== "administrative")
      .sort((a, b) => {
        const score = (r: any) =>
          ["mall", "shop", "amenity", "building", "commercial"].includes(r.class)
            ? 0
            : 1;
        return score(a) - score(b);
      })
      .map(r => ({
        label: r.display_name,
        lat: Number(r.lat),
        lng: Number(r.lon),
      }));

  } catch (err) {
    return [];
  }
}
