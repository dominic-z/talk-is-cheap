
export default function () {

    let objs = [{ name: 'zz' }, { name: 'bb' }, { name: 'cc' }]

    let divs = objs.map(obj => {
        return <div>{obj.name}</div>
    })

    console.log(divs)
    return <>
        {divs}
    </>
}