import axios from "axios";
import cheerio from "cheerio";

async function scrapeRyans(query) {
  const url = `https://www.ryans.com/search?q=${query}`;
  const products = [];

  try {
    const { data } = await axios.get(url);
    const $ = cheerio.load(data);

    $(".product-box").each((_, element) => {
      const name = $(element).find(".product-title a").text().trim();
      const price = parseFloat(
        $(element)
          .find(".price")
          .text()
          .replace(/[^0-9.]/g, "")
      );
      const link = $(element).find(".product-title a").attr("href");

      products.push({
        source: "Ryans",
        name,
        price,
        link: `https://www.ryanscomputers.com${link}`,
      });
    });

    return products;
  } catch (error) {
    console.error(`Error scraping Ryans: ${error.message}`);
    return [];
  }
}

module.exports = { scrapeRyans };
